package com.example.service;

import com.example.model.Channel;
import com.example.model.Invitation;
import com.example.model.User;
import com.example.model.UserChannel;
import com.example.repository.InvitationRepository;
import com.example.repository.UserChannelRepository;
import com.example.repository.ChannelRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des utilisateurs
 * Gère les opérations de base sur les utilisateurs qui sont utilisées dans le controller
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;
    private final InvitationRepository invitationRepository;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserChannelRepository userChannelRepository,
            ChannelRepository channelRepository,
            InvitationRepository invitationRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userChannelRepository = userChannelRepository;
        this.channelRepository = channelRepository;
        this.invitationRepository = invitationRepository;
    }

    public User createUser(String firstName, String lastName, String email, String password, boolean isAdmin) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Hachage du mot de passe
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(hashedPassword);  // Le mot de passe est haché avant d'être enregistré
        user.setAdmin(isAdmin);

        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, String firstName, String lastName, String email, boolean isAdmin, String avatar) {
        // Validation des champs obligatoires
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est obligatoire");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }

        // Vérifier si l'email existe déjà pour un autre utilisateur
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(id)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // Récupération de l'utilisateur existant
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur n'existe pas"));

        // Mise à jour des champs
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setAdmin(isAdmin);
        user.setAvatar(avatar);

        // Sauvegarde de l'utilisateur
        return userRepository.save(user);
    }

    public User updateUserAvatar(Long id, String avatar) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User n'existe pas"));
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Vérification du mot de passe pour l'authentification
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public Long getUserId(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User n'existe pas"));
        return user.getUserId();
    }

    public List<Channel> getUserChannels(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<UserChannel> userChannels = userChannelRepository.findByUser(user);
        return userChannels.stream()
                .map(UserChannel::getChannel)
                .filter(channel -> channel.getDate().isAfter(LocalDateTime.now().minusDays(1))) //affiche les channels passés de 1 jour
                .collect(Collectors.toList());
    }

    public List<Channel> getUserChannelsOwner(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Channel> channels = channelRepository.findByOwner(user);
        return channels;
    }

    public Optional<UserChannel> findByUserAndChannel(User user, Channel channel) { //ça utilise JPA pour la jointure
        return userChannelRepository.findByUserAndChannel(user, channel);
    }

    // Get user invitations
    public List<Invitation> getUserInvites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User n'existe pas"));
        return invitationRepository.findByUserAndStatus(user, "pending");
    }

    // Can user join channel
    public boolean canUserJoinChannel(User user, Channel channel) {
        // Vérifie si l'utilisateur est déjà membre du channel
        if (userChannelRepository.existsByUserAndChannel(user, channel)) {
            throw new IllegalStateException("L'utilisateur a déjà rejoint ce channel");
        }

        // Vérifie si une invitation existe
        if (!invitationRepository.existsByUserAndChannel(user, channel)) {
            throw new IllegalStateException("Aucune invitation trouvée pour rejoindre ce channel");
        }

        return true;
    }

    @Transactional
    public void acceptInvitation(Long invitationId) {
        // Récupérer l'invitation par son ID
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation n'existe pas"));

        // Extraire l'utilisateur et le channel associés à l'invitation
        User user = invitation.getUser();
        Channel channel = invitation.getChannel();

        userChannelRepository.existsByUserAndChannel(user, channel);

        // Vérifie si l'utilisateur est déjà membre du channel
        if (userChannelRepository.existsByUserAndChannel(user, channel)) {
            throw new IllegalStateException("L'utilisateur est déjà membre du channel.");
        }

        // Ajoute l'utilisateur au channel
        UserChannel userChannel = new UserChannel();
        userChannel.setUser(user);
        userChannel.setChannel(channel);
        userChannelRepository.save(userChannel);

        // Masque l'invitation
        invitation.setStatus("accepted");
        invitationRepository.save(invitation);
    }

    @Transactional
    public void declineInvitation(Long invitationId) {
        // Récupérer l'invitation par son ID
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation n'existe pas"));

        // Supprime l'invitation
        invitationRepository.delete(invitation);
    }

    @Transactional
    public void sendInvitation(Long userId, Long channelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User n'existe pas"));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel n'existe pas"));

        // Vérifie si l'invitation existe déjà
        Invitation invitationTemp = invitationRepository.findByUserAndChannel(user, channel).orElse(null);
        if (invitationTemp != null && invitationTemp.getStatus().equals("pending")) {
            throw new IllegalArgumentException("Invitation est déjà en attente");
        }
        if (invitationTemp != null && invitationTemp.getStatus().equals("accepted")) {
            throw new IllegalArgumentException("Invitation est déjà acceptée");
        }

        // Crée et sauvegarde l'invitation
        Invitation invitation = new Invitation();
        invitation.setUser(user);
        invitation.setChannel(channel);
        invitation.setStatus("pending");
        invitationRepository.save(invitation);
    }
}
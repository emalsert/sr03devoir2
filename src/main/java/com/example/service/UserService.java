package com.example.service;

import com.example.model.Channel;
import com.example.model.User;
import com.example.model.UserChannel;
import com.example.repository.UserChannelRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserChannelRepository userChannelRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserChannelRepository userChannelRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userChannelRepository = userChannelRepository;
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

    public User updateUser(Long id, String firstName, String lastName, String email, boolean isAdmin) {
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
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Mise à jour des champs
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setAdmin(isAdmin);

        // Sauvegarde de l'utilisateur
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

    public List<Channel> getUserChannels(Long userId) { //j'ai utilise la fonction d'après (au final non le prof aime pas donc on reste là)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<UserChannel> userChannels = userChannelRepository.findByUser(user);
        return userChannels.stream()
                .map(UserChannel::getChannel)
                .collect(Collectors.toList());
    }

    public Optional<UserChannel> findByUserAndChannel(User user, Channel channel) { //ça utilise JPA pour la jointure
        return userChannelRepository.findByUserAndChannel(user, channel);
    }

    //get user invitations

    //Can user join channel
}
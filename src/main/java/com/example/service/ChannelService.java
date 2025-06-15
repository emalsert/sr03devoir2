package com.example.service;

import com.example.model.Channel;
import com.example.model.Invitation;
import com.example.model.User;
import com.example.model.UserChannel;
import com.example.repository.ChannelRepository;
import com.example.repository.InvitationRepository;
import com.example.repository.UserChannelRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des canaux
 * Gère les opérations de base sur les canaux qui sont utilisées dans le controller
 */
@Service
@Transactional
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final UserChannelRepository userChannelRepository;
    private final InvitationRepository invitationRepository;

    @Autowired
    public ChannelService(
            ChannelRepository channelRepository,
            UserRepository userRepository,
            UserChannelRepository userChannelRepository,
            InvitationRepository invitationRepository
    ) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.userChannelRepository = userChannelRepository;
        this.invitationRepository = invitationRepository;
    }

    /**
     * Crée un canal en faisant les vérifications nécessaires
     * @return le canal créé
     */
    public Channel createChannel(String title, String description, LocalDateTime date, Integer durationMinutes, Long ownerId) {
        // Validation des champs obligatoires
        if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Le titre du channel est obligatoire");
        }
        if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("La description du channel est obligatoire");
        }
        if (date == null) {
                throw new IllegalArgumentException("La date du channel est obligatoire");
        }
        if (durationMinutes <= 0) {
                throw new IllegalArgumentException("La durée du channel doit être positive");
        }
        if (ownerId == null) {
                throw new IllegalArgumentException("Le propriétaire du channel est obligatoire");
        }

        // Validation de la date (doit être dans le futur)
        if (date.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date du channel doit être dans le futur");
        }

        // Récupération et validation de l'owner
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // Création du channel
        Channel channel = new Channel();
        channel.setTitle(title);
        channel.setDescription(description);
        channel.setDate(date);
        channel.setDurationMinutes(durationMinutes);
        channel.setOwner(owner);

        // Sauvegarde du channel
        Channel savedChannel = channelRepository.save(channel);

        // Créer l'entrée dans la table user_channel
        UserChannel userChannel = new UserChannel();
        userChannel.setUser(owner);
        userChannel.setChannel(savedChannel);
        userChannelRepository.save(userChannel);

        return savedChannel;
    }

    public Optional<Channel> getChannelById(Long id) {
        return channelRepository.findById(id);
    }

    public List<Channel> getChannelsByOwner(User owner) {
        return channelRepository.findByOwner(owner);
    }

    public List<Channel> getUpcomingChannels() {
        return channelRepository.findByDateAfter(LocalDateTime.now());
    }

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    public List<Channel> getChannelsBetween(LocalDateTime start, LocalDateTime end) {
        return channelRepository.findByDateBetween(start, end);
    }

    public Channel updateChannel(Long id, String title, String description, LocalDateTime date, Integer durationMinutes) {
        // Validation des champs obligatoires
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du channel est obligatoire");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description du channel est obligatoire");
        }
        if (date == null) {
            throw new IllegalArgumentException("La date du channel est obligatoire");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("La durée du channel doit être positive");
        }
        if (date.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date du channel doit être dans le futur");
        }

        // Récupération du channel existant
        Channel channel = channelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        // Mise à jour des champs
        channel.setTitle(title);
        channel.setDescription(description);
        channel.setDate(date);
        channel.setDurationMinutes(durationMinutes);

        // Sauvegarde du channel
        return channelRepository.save(channel);
    }

    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }

    // Get channel invitations
    public List<Invitation> getChannelInvites(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        return invitationRepository.findByChannel(channel);
    }

    // Retourne les ids des utilisateurs d'un channel
    public List<Long> getChannelUserIds(Long channelId) {
        // Vérifier si le channel existe
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        // Récupérer toutes les entrées user_channel pour ce canal
        List<UserChannel> userChannels = userChannelRepository.findByChannel(channel);

        // Transformer la liste de UserChannel en liste d'IDs utilisateurs
        return userChannels.stream()
                .map(userChannel -> userChannel.getUser().getUserId())
                .toList();
    }
}

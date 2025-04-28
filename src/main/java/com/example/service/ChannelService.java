package com.example.service;

import com.example.model.Channel;
import com.example.model.User;
import com.example.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final UserService userService;

    @Autowired
    public ChannelService(ChannelRepository channelRepository, UserService userService) {
        this.channelRepository = channelRepository;
        this.userService = userService;
    }

    public Channel createChannel(String title, String description, LocalDateTime date, int duration, Long ownerId) {
        // Validation des champs obligatoires
        String errorMessage = null;
        
        if (title == null || title.trim().isEmpty()) {
            errorMessage = "Le titre du channel est obligatoire";
        } else if (description == null || description.trim().isEmpty()) {
            errorMessage = "La description du channel est obligatoire";
        } else if (date == null) {
            errorMessage = "La date du channel est obligatoire";
        } else if (duration <= 0) {
            errorMessage = "La durée du channel doit être positive";
        } else if (ownerId == null) {
            errorMessage = "Le propriétaire du channel est obligatoire";
        } else if (date.isBefore(LocalDateTime.now())) {
            errorMessage = "La date du channel doit être dans le futur";
        }

        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }

        // Récupération et validation de l'owner
        User owner = userService.getUserById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // Création du channel
        Channel channel = new Channel();
        channel.setTitle(title);
        channel.setDescription(description);
        channel.setDate(date);
        channel.setDuration(Duration.ofHours(duration));
        channel.setOwner(owner);

        // Sauvegarde du channel
        return channelRepository.save(channel);
    }

    public Channel updateChannel(Long id, String title, String description, LocalDateTime date, Duration duration) {
        // Validation des champs obligatoires
        String errorMessage = null;
        
        if (title == null || title.trim().isEmpty()) {
            errorMessage = "Le titre du channel est obligatoire";
        } else if (description == null || description.trim().isEmpty()) {
            errorMessage = "La description du channel est obligatoire";
        } else if (date == null) {
            errorMessage = "La date du channel est obligatoire";
        } else if (duration.toMinutes() <= 0) {
            errorMessage = "La durée du channel doit être positive";
        } else if (date.isBefore(LocalDateTime.now())) {
            errorMessage = "La date du channel doit être dans le futur";
        }

        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }
        
        Channel channel = channelRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        channel.setTitle(title);
        channel.setDescription(description);
        channel.setDate(date);
        channel.setDuration(duration);
        return channelRepository.save(channel);
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

    public List<Channel> getChannelsBetween(LocalDateTime start, LocalDateTime end) {
        return channelRepository.findByDateBetween(start, end);
    }

    public Channel updateChannel(Channel channel) {
        return channelRepository.save(channel);
    }

    public void deleteChannel(Long id) {
        channelRepository.deleteById(id);
    }
} 
package com.example.service;

import com.example.model.Channel;
import com.example.model.User;
import com.example.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChannelService {
    private final ChannelRepository channelRepository;

    @Autowired
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public Channel createChannel(Channel channel) {
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
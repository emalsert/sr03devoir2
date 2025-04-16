package com.example.repository;

import com.example.model.Channel;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByOwner(User owner);
    List<Channel> findByDateAfter(LocalDateTime date);
    List<Channel> findByDateBetween(LocalDateTime start, LocalDateTime end);
} 
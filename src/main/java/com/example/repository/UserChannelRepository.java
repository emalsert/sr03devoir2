package com.example.repository;

import com.example.model.Channel;
import com.example.model.User;
import com.example.model.UserChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannel, Long> {
    List<UserChannel> findByUser(User user);
    List<UserChannel> findByChannel(Channel channel);
    Optional<UserChannel> findByUserAndChannel(User user, Channel channel);
    boolean existsByUserAndChannel(User user, Channel channel);
} 
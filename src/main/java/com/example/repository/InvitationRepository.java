package com.example.repository;

import com.example.model.Channel;
import com.example.model.Invitation;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByUserAndStatus(User user, String status);

    List<Invitation> findByChannel(Channel channel);
    Optional<Invitation> findByUserAndChannel(User user, Channel channel);
    boolean existsByUserAndChannel(User user, Channel channel);
}

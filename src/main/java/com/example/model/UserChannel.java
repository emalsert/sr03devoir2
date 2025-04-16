package com.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_channel")
public class UserChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userChannelId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
} 
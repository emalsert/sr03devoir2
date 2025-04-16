package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Lob
    private byte[] avatar;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @Column(nullable = false)
    private boolean isConnected = false;

    @OneToMany(mappedBy = "user")
    private Set<UserChannel> userChannels = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Invitation> invitations = new HashSet<>();
} 
package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore; //pour ignorer les champs dans les réponses JSON

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

    @Column(nullable = true)
    private String avatar;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<UserChannel> userChannels = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Invitation> invitations = new HashSet<>();

    //on delete cascade les tables liées
} 
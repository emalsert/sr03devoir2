package com.example.controller;

import com.example.model.User;
import com.example.model.Channel;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des utilisateurs côté client (RestController API)
 * Gère les requêtes de gestion des utilisateurs
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    /**
     * Récupère les canaux d'un utilisateur
     * @param userId Identifiant de l'utilisateur
     * @return la liste des canaux de l'utilisateur
     */
    @GetMapping("/{userId}/channels")
    public ResponseEntity<List<Channel>> getUserChannels(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserChannels(userId));
    }

    /**
     * Récupère les canaux d'un utilisateur propriétaire
     * @param userId Identifiant de l'utilisateur
     * @return la liste des canaux de l'utilisateur propriétaire
     */
    @GetMapping("/{userId}/channels/owner")
    public ResponseEntity<List<Channel>> getUserChannelsOwner(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserChannelsOwner(userId));
    }

    /**
     * Récupère tous les utilisateurs
     * @return la liste de tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Récupère un utilisateur par son identifiant
     * @param userId Identifiant de l'utilisateur
     * @return l'utilisateur
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Met à jour un utilisateur
     * @param userId Identifiant de l'utilisateur
     * @param user Utilisateur à mettre à jour
     * @return l'utilisateur mis à jour
     */
    @PutMapping("/{userId}/edit")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(userId, user.getFirstName(), user.getLastName(), user.getEmail(), user.isAdmin(), user.getAvatar()));
    }
}
 
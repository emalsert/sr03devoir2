package com.example.controller;

import com.example.model.Channel;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.service.JwtService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur pour la gestion des canaux côté client (RestController API)
 * Gère les requêtes de gestion des canaux
 */
@RestController
@RequestMapping("/api/channels")
public class ChannelRestController {
    
    @Autowired
    private ChannelService channelService;

    @Autowired
    private JwtService jwtService;

    /**
     * Récupère la liste des canaux à venir
     * @return la liste des canaux à venir
     */
    @GetMapping
    public ResponseEntity<List<Channel>> getUpcomingChannels() {
        return ResponseEntity.ok(channelService.getUpcomingChannels());
    }

    /**
     * Crée un canal
     * @param title Titre du canal
     * @param description Description du canal
     * @param date Date de début du canal
     * @param durationMinutes Durée du canal en minutes
     * @param ownerId Identifiant de l'utilisateur propriétaire du canal
     * @return le canal créé
     */
    @PostMapping("/create")
    public ResponseEntity<?> createChannel(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int durationMinutes,
            @RequestParam Long ownerId) {
        try {
            Channel channel = channelService.createChannel(title, description, date, durationMinutes, ownerId);
            return ResponseEntity.ok("Canal créé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère un canal par son identifiant
     * @param id Identifiant du canal
     * @return le canal
     */
    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannel(@PathVariable Long id) {
        return channelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Met à jour un canal
     * @param id Identifiant du canal
     * @param title Titre du canal
     * @param description Description du canal
     * @param date Date de début du canal
     * @param durationMinutes Durée du canal en minutes
     * @param token Token JWT
     * @return le canal mis à jour
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateChannel(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int durationMinutes,
            @RequestHeader("Authorization") String token) {
        try {
            //on vérifie que 'user est le même que celui du owner de channel
            String username = jwtService.extractUsername(token);
            if (username == null) {
                return ResponseEntity.badRequest().body("Pas owner");
            }
            
            Channel channel = channelService.updateChannel(id, title, description, date, durationMinutes);
            return ResponseEntity.ok(channel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Supprime un canal
     * @param id Identifiant du canal
     * @return la réponse HTTP
     */
    @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteChannel(@PathVariable Long id) {
        try {
            channelService.deleteChannel(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 
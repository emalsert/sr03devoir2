package com.example.controller;

import com.example.model.Invitation;
import com.example.model.User;
import com.example.model.Channel;
import com.example.service.UserService;
import com.example.service.ChannelService;
import com.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des invitations côté client (RestController API)
 * Gère les requêtes de gestion des invitations
 */
@RestController
@RequestMapping("/api/invitations")
public class InvitesRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EmailService emailService;

    // Récupérer toutes les invitations d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Invitation>> getInvitationsForUser(@PathVariable Long userId) {
        try {
            List<Invitation> invitations = userService.getUserInvites(userId);

            return ResponseEntity.ok(invitations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Accepte une invitation
     * @param invitationId Identifiant de l'invitation
     * @return la réponse HTTP
     */
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Long invitationId) {

        try {
            // Appel du service pour accepter l'invitation
            userService.acceptInvitation(invitationId);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Refuse une invitation
     * @param invitationId Identifiant de l'invitation
     * @return la réponse HTTP
     */
    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(
            @PathVariable Long invitationId) {
        try {
            // Appel du service pour refuser l'invitation
            userService.declineInvitation(invitationId);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Envoie une invitation
     * @param userId Identifiant de l'utilisateur
     * @param channelId Identifiant du canal
     * @param inviterId Identifiant de l'utilisateur qui envoie l'invitation
     * @return la réponse HTTP
     */
    @PostMapping("/invite")
    public ResponseEntity<?> sendInvitation(
            @RequestParam Long userId,
            @RequestParam Long channelId,
            @RequestParam Long inviterId) {
        try {
            userService.sendInvitation(userId, channelId);
            
            // Récupérer les informations nécessaires pour l'email
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User n'existe pas"));
            Channel channel = channelService.getChannelById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel n'existe pas"));
            User inviter = userService.getUserById(inviterId)
                    .orElseThrow(() -> new IllegalArgumentException("Inviter n'existe pas"));
            
            // Envoyer l'email d'invitation avec le template
            emailService.sendEmailInvitation(
                user.getEmail(), 
                channel.getTitle(), 
                inviter.getFirstName() + " " + inviter.getLastName()
            );
            
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

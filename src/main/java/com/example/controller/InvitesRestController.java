package com.example.controller;

import com.example.model.Invitation;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "http://localhost:3000")
public class InvitesRestController {

    @Autowired
    private UserService userService;

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

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable Long invitationId,
            @RequestParam Long channelId) {
        // Début de la méthode accept
        System.out.println("Début de acceptInvitation: invitationId = " + invitationId + ", channelId = " + channelId);

        try {
            // Appel du service pour accepter l'invitation
            userService.acceptInvitation(invitationId);

            // Si tout s'est bien passé, afficher un message de succès
            System.out.println("Invitation acceptée avec succès pour invitationId = " + invitationId + " et channelId = " + channelId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // En cas d'erreur, afficher un message d'échec
            System.out.println("Erreur lors de l'acceptation de l'invitation : " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{invitationId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long invitationId,
            @RequestParam Long channelId) {  // Le deuxième paramètre ajouté
        // Début de la méthode decline
        System.out.println("Début de declineInvitation: invitationId = " + invitationId + ", channelId = " + channelId);

        try {
            // Appel du service pour refuser l'invitation
            userService.declineInvitation(invitationId);

            // Si tout s'est bien passé, afficher un message de succès
            System.out.println("Invitation refusée avec succès pour invitationId = " + invitationId + " et channelId = " + channelId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // En cas d'erreur, afficher un message d'échec
            System.out.println("Erreur lors du refus de l'invitation : " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

package com.example.controller;

import com.example.model.User;
import com.example.model.Channel;
import com.example.repository.InvitationRepository;
import com.example.service.UserService;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Contrôleur pour la gestion des administrateurs
 * Gère les requêtes de gestion des utilisateurs, des canaux et des invitations
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final ChannelService channelService;
    private final InvitationRepository invitationRepository;

    /**
     * Constructeur pour initialiser les services
     * @param userService Service pour la gestion des utilisateurs
     * @param channelService Service pour la gestion des canaux
     * @param invitationRepository Service pour la gestion des invitations
     */
    @Autowired
    public AdminController(
            UserService userService,
            ChannelService channelService,
            InvitationRepository invitationRepository
    ) {
        this.userService = userService;
        this.channelService = channelService;
        this.invitationRepository = invitationRepository;
    }

    /**
     * Page de tableau de bord pour les administrateurs
     * @param model Modèle pour la vue
     * @return la page de tableau de bord
     */
    @GetMapping
    public String adminDashboard(Model model) {
        try {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("channels", channelService.getUpcomingChannels());
            return "admin/dashboard";
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès au dashboard admin", e);
            return "error";
        }
    }

    /**
     * Page de gestion des utilisateurs
     * @param model Modèle pour la vue
     * @return la page de gestion des utilisateurs
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            model.addAttribute("users", userService.getAllUsers());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("username", auth.getName());

            return "admin/users";
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès à la gestion des utilisateurs", e);
            return "error";
        }
    }

    /**
     * Page de gestion des canaux
     * @param model Modèle pour la vue
     * @return la page de gestion des canaux
     */
    @GetMapping("/channels")
    public String manageChannels(Model model) {
        try {
            model.addAttribute("channels", channelService.getAllChannels());
            model.addAttribute("users", userService.getAllUsers());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("username", auth.getName());

            return "admin/channels";
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès à la gestion des canaux", e);
            return "error";
        }
    }

    /**
     * Page de gestion des invitations
     * @param model Modèle pour la vue
     * @return la page de gestion des invitations
     */
    @GetMapping("/invites")
    public String manageInvites(Model model) {
        try {
            model.addAttribute("invitations", invitationRepository.findAll());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("username", auth.getName());

            return "admin/invites";  // Vue pour gérer les invitations
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès à la gestion des invitations", e);
            return "error";
        }
    }

    /**
     * Suppression d'une invitation
     * @param id Identifiant de l'invitation
     * @param model Modèle pour la vue
     * @return la page de gestion des invitations
     */
    @DeleteMapping("/invites/{id}")
    public String deleteInvitation(@PathVariable Long id, Model model) {
        try {
            invitationRepository.deleteById(id);
            return "redirect:/admin/invites";
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du channel", e);
            model.addAttribute("error", "Une erreur est survenue lors de la suppression du channel");
            return "admin/invites";
        }
    }

    /**
     * Page de test
     * @return la page de test
     */
    @GetMapping("/test")
    public String testPage() {
        return "admin/test";
    }

    /**
     * Création d'un canal
     * @param title Titre du canal
     * @param description Description du canal
     * @param date Date de début du canal
     * @param durationMinutes Durée du canal en minutes
     * @param ownerId Identifiant de l'utilisateur propriétaire du canal
     * @param model Modèle pour la vue
     * @return la page de gestion des canaux
     */
    @PostMapping("/channels")
    public String createChannel(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam("duration") int durationMinutes,
            @RequestParam("ownerId") Long ownerId,
            Model model) {
        try {
            channelService.createChannel(
                title,
                description,
                LocalDateTime.parse(date),
                durationMinutes,
                ownerId
            );
            return "redirect:/admin/channels";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("channels", channelService.getUpcomingChannels());
            model.addAttribute("users", userService.getAllUsers());
            return "admin/channels";
        } catch (Exception e) {
            logger.error("Erreur lors de la création du channel", e);
            model.addAttribute("error", "Une erreur est survenue lors de la création du channel");
            model.addAttribute("users", userService.getAllUsers());
            return "admin/channels";
        }
    }

    /**
     * Mise à jour d'un canal
     * @param id Identifiant du canal
     * @param title Titre du canal
     * @param description Description du canal
     * @param date Date de début du canal
     * @param durationMinutes Durée du canal en minutes
     * @param model Modèle pour la vue
     * @return la page de gestion des canaux
     */
    @PutMapping("/channels/{id}")
    public String updateChannel(
        @PathVariable Long id,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("date") String date,
        @RequestParam("duration") int durationMinutes,
        Model model) {
        try {
            channelService.updateChannel(id, title, description, LocalDateTime.parse(date), durationMinutes);
            return "redirect:/admin/channels";
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du channel", e);
            model.addAttribute("error", "Une erreur est survenue lors de la mise à jour du channel");
            return "admin/channels";
        }
    }

    /**
     * Suppression d'un canal
     * @param id Identifiant du canal
     * @param model Modèle pour la vue
     * @return la page de gestion des canaux
     */
    @DeleteMapping("/channels/{id}")
    public String deleteChannel(@PathVariable Long id, Model model) {
        try {
            channelService.deleteChannel(id);
            return "redirect:/admin/channels";
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du channel", e);
            model.addAttribute("error", "Une erreur est survenue lors de la suppression du channel");
            return "admin/channels";
        }
    }


    /**
     * Création d'un utilisateur
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de l'utilisateur
     * @param email Adresse email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @param isAdmin Indique si l'utilisateur est administrateur
     * @param model Modèle pour la vue
     * @return la page de gestion des utilisateurs
     */
    @PostMapping("/users")
    public String createUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("isAdmin") boolean isAdmin,
            Model model) {
        try {
            userService.createUser(firstName, lastName, email, password, isAdmin);
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users";
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur", e);
            model.addAttribute("error", "Une erreur est survenue lors de la création de l'utilisateur");
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users";
        }
    }

    /**
     * Suppression d'un utilisateur
     * @param id Identifiant de l'utilisateur
     * @param model Modèle pour la vue
     * @return la page de gestion des utilisateurs
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, Model model) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue lors de la suppression de l'utilisateur.");
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users";
        }
    }

    /**
     * Mise à jour d'un utilisateur
     * @param id Identifiant de l'utilisateur
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de l'utilisateur
     * @param email Adresse email de l'utilisateur
     * @param isAdmin Indique si l'utilisateur est administrateur
     * @param avatar Avatar de l'utilisateur
     * @param model Modèle pour la vue
     * @return la page de gestion des utilisateurs
     */
    @PutMapping("/users/{id}")  
    public String updateUser(
        @PathVariable Long id,
        @RequestParam("firstName") String firstName,
        @RequestParam("lastName") String lastName,
        @RequestParam("email") String email,
        @RequestParam("isAdmin") boolean isAdmin,
        @RequestParam("avatar") String avatar,
        Model model) {
        try {
            userService.updateUser(id, firstName, lastName, email, isAdmin, avatar);
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur", e);
            model.addAttribute("error", "Une erreur est survenue lors de la mise à jour de l'utilisateur");
            model.addAttribute("users", userService.getAllUsers());
            return "admin/users";
        }
    }
} 
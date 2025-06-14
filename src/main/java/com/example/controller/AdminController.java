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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final ChannelService channelService;
    private final InvitationRepository invitationRepository;

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

    @GetMapping("/test")
    public String testPage() {
        return "admin/test";
    }

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
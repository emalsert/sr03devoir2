package com.example.controller;

import com.example.model.User;
import com.example.model.Channel;
import com.example.service.UserService;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AdminController(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
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
            return "admin/users";
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès à la gestion des utilisateurs", e);
            return "error";
        }
    }

    @GetMapping("/channels")
    public String manageChannels(Model model) {
        try {
            model.addAttribute("channels", channelService.getUpcomingChannels());
            model.addAttribute("users", userService.getAllUsers());
            return "admin/channels";
        } catch (Exception e) {
            logger.error("Erreur lors de l'accès à la gestion des canaux", e);
            return "error";
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
            @RequestParam("duration") int duration,
            @RequestParam("ownerId") Long ownerId,
            Model model) {
        try {
            channelService.createChannel(
                title,
                description,
                LocalDateTime.parse(date),
                duration,
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
} 
package com.example.controller;

import com.example.model.User;
import com.example.model.Channel;
import com.example.service.UserService;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
} 
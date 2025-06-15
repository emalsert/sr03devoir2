package com.example.controller;

import com.example.model.User;
import com.example.service.JwtService;
import com.example.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour la gestion de l'authentification des administrateurs
 * Gère les requêtes de connexion, de déconnexion et de gestion du tableau de bord
 */
@Controller
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Constructeur pour initialiser les services
     * @param authenticationManager Gestionnaire d'authentification
     * @param userService Service pour la gestion des utilisateurs
     * @param jwtService Service pour la gestion des tokens JWT
     * @param userDetailsService Service pour charger les détails utilisateur
     */
    public AdminAuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Page de connexion pour les administrateurs
     * @return la page de connexion
     */
    @GetMapping("/admin/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    /**
     * Traitement de la connexion pour les administrateurs
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     * @param response Réponse HTTP
     * @return la page de tableau de bord ou la page de connexion en cas d'erreur
     */
    @PostMapping("/admin/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password,
                       HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            User user = userService.getUserByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isAdmin()) {
                return "redirect:/admin/login?error=unauthorized";
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(user);

            // Créer un cookie avec le token JWT
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 heures
            response.addCookie(jwtCookie);

            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            return "redirect:/admin/login?error";
        }
    }

    /**
     * Page de tableau de bord pour les administrateurs
     * @param model Modèle pour la vue
     * @return la page de tableau de bord
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        return "admin/dashboard";
    }

    /**
     * Traitement de la déconnexion pour les administrateurs
     * @param response Réponse HTTP
     * @return la page de connexion avec un message de déconnexion
     */
    @GetMapping("/admin/logout")
    public String logout(HttpServletResponse response) {
        // Supprimer le cookie JWT
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return "redirect:/admin/login?logout";
    }
} 
package com.example.controller;

import com.example.model.User;
import com.example.service.JwtService;
import com.example.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la gestion de l'authentification côté client (RestController API)
 * Gère les requêtes de connexion, de déconnexion et de gestion des utilisateurs
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Récupère les informations de l'utilisateur connecté
     * @return l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Not authenticated");
            }

            String email = authentication.getName();
            User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    /**
     * Connexion d'un utilisateur
     * @param request Requête de connexion
     * @param response Réponse HTTP
     * @return l'utilisateur connecté
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );

            User user = userService.getUserByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtService.generateToken(user);

            // Créer le cookie avec le token JWT
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(false); // pour pouvoir l'extraire côté client pour l'envoyer via websocket
            jwtCookie.setSecure(false); // on est en http pour le moment
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 heures
            response.addCookie(jwtCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", user);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    /**
     * Inscription d'un utilisateur
     * @param request Requête d'inscription
     * @param response Réponse HTTP
     * @return l'utilisateur inscrit
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            if (userService.existsByEmail(request.email())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            User user = userService.createUser(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                false
            );

            String token = jwtService.generateToken(user);

            // Créer le cookie avec le token JWT
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(false);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 heures
            response.addCookie(jwtCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", user);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Déconnexion d'un utilisateur
     * @param response Réponse HTTP
     * @return la réponse HTTP
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Supprimer le cookie JWT
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immédiatement
        jwtCookie.setDomain("localhost");
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().build();
    }
}

record LoginRequest(String email, String password) {}
record RegisterRequest(String firstName, String lastName, String email, String password) {} 
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

@Controller
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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

    @GetMapping("/admin/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

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

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        return "admin/dashboard";
    }

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
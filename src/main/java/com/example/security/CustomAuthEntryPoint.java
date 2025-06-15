package com.example.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Point d'entrée personnalisé pour gérer les tentatives d'accès non authentifié
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String accept = request.getHeader("Accept");

        // Redirection vers la page de connexion appropriée pour les requêtes HTML
        if (accept != null && accept.contains("text/html")) {
            response.sendRedirect("/admin/login");
        } else {
            // Pour les clients API, renvoyer 401 sans redirection
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}

package com.example.config;

import com.example.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Optional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtre d'authentification JWT pour Spring Security
 * Vérifie les tokens JWT dans les cookies et définit l'authentification
 * dans le contexte de sécurité
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service pour gérer les tokens JWT
     */
    private final JwtService jwtService;

    /**
     * Service pour charger les détails utilisateur
     */
    private final UserDetailsService userDetailsService;

    /**
     * Filtre pour vérifier les tokens JWT dans les cookies
     * et définir l'authentification dans le contexte de sécurité
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Récupère le token JWT depuis les cookies
        final String jwt = getJwtFromCookie(request);
        final String userEmail;
        
        // Si aucun token n'est trouvé, passe au filtre suivant
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait l'email de l'utilisateur du token
        userEmail = jwtService.extractUsername(jwt);
        
        // Si l'utilisateur n'est pas déjà authentifié, vérifie le token
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Charge les détails utilisateur
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            // Vérifie si le token est valide
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Extrait les revendications du token
                Claims claims = jwtService.extractAllClaims(jwt);
                
                // Récupère les rôles de l'utilisateur
                List<String> roles = claims.get("roles", List.class);
                List<GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Crée un token d'authentification
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities
                );
                
                // Définit les détails de l'authentification
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Définit l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Récupère le token JWT depuis les cookies
     * @param request la requête HTTP
     * @return le token JWT ou null si non trouvé
     */
    private String getJwtFromCookie(HttpServletRequest request) {
        // Récupère les cookies de la requête
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // Recherche le cookie "jwt"
        Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                .filter(cookie -> "jwt".equals(cookie.getName()))
                .findFirst();

        // Retourne la valeur du cookie ou null si non trouvé
        return jwtCookie.map(Cookie::getValue).orElse(null);
    }
} 
package com.example.config;

import com.example.security.CustomAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de la sécurité pour l'application
 * Définit les filtres d'authentification, les fournisseurs d'authentification
 * et les autorisations pour les différentes routes
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Filtre d'authentification JWT pour Spring Security
     */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Service pour charger les détails utilisateur
     */
    private final UserDetailsService userDetailsService;

    /**
     * Point d'entrée personnalisé pour la gestion des erreurs d'authentification
     */
    private final CustomAuthEntryPoint customAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Ressources statiques et routes système
                .requestMatchers("/error", "/favicon.ico").permitAll()
                .requestMatchers("/css/**", "/js/**", "/webjars/**", "/images/**").permitAll()
                
                // Routes publiques
                .requestMatchers("/admin/login", "/login").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                
                .requestMatchers("/ws/**").permitAll()  // Autoriser les connexions WebSocket

                // Routes admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Routes API
                .requestMatchers("/api/**").authenticated()
                
                // Toutes les autres routes
                .anyRequest().authenticated()
            )
            // Dans le cas ou l'utilisateur n'est pas connecté, le rediriger vers customAuthEntryPoint
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthEntryPoint)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 
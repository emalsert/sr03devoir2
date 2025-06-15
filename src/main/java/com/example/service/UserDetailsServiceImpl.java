package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des utilisateurs
 * Gère la connexion des utilisateurs
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return les informations de l'utilisateur
     * @throws UsernameNotFoundException si l'utilisateur n'est pas trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Tentative de connexion avec l'email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé pour l'email: {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé");
                });
        
        logger.info("Utilisateur trouvé: {}", user.getEmail());


        // On récupère les roles de l'utilisateur (champs isAdmin)
        String[] roles = user.isAdmin() ? new String[]{"ADMIN"} : new String[]{"USER"};
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
} 
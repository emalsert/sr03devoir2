package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration du WebSocket pour l'application de chat.
 * Cette classe configure le système de messagerie en temps réel avec STOMP.
 * 
 * Principales responsabilités :
 * 1. Configuration du broker de messages STOMP
 * 2. Définition des endpoints WebSocket
 * 3. Configuration des préfixes de destination
 * 4. Gestion des CORS et de la compatibilité navigateur
 */
@Configuration
@EnableWebSocketMessageBroker  // Active le support des messages WebSocket avec STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${URL_FRONTEND}")
    private String urlFrontend;

    /**
     * Configure le broker de messages STOMP.
     * Définit deux types de destinations :
     * - /app/* : Pour les messages envoyés par les clients
     * - /topic/* : Pour les messages reçus par les clients
     * 
     * Exemple de flux :
     * 1. Client envoie un message à /app/chat/123/send
     * 2. Serveur traite le message
     * 3. Serveur diffuse le message à /topic/chat/123
     * 4. Tous les clients abonnés à /topic/chat/123 reçoivent le message
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les destinations où les clients peuvent envoyer des messages
        // Les messages envoyés à /app/* seront routés vers les méthodes @MessageMapping
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les destinations où les clients peuvent s'abonner
        // Les messages envoyés à /topic/* seront diffusés aux clients abonnés
        config.enableSimpleBroker("/topic");
    }

    /**
     * Configure les endpoints WebSocket.
     * Définit l'URL d'endpoint et les paramètres de connexion.
     * 
     * Points importants :
     * - Endpoint : /ws
     * - CORS : autorise uniquement localhost:3000 (frontend React)
     * - SockJS : activé pour la compatibilité avec les navigateurs plus anciens
     * 
     * Exemple de connexion client :
     * const socket = new SockJS('http://localhost:8080/ws');
     * const stompClient = new Client({ webSocketFactory: () => socket });
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // URL d'endpoint pour la connexion WebSocket
                .setAllowedOrigins(urlFrontend)  // Configuration CORS pour le frontend
                .withSockJS();  // Support des navigateurs plus anciens via SockJS
    }
} 
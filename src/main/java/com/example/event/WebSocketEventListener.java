package com.example.event;

import com.example.service.ChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.example.service.UserService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Composant qui écoute les événements de connexion/déconnexion WebSocket
 * Il gère l'ajout et la suppression des utilisateurs dans les salons de chat
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    // Service qui gère la logique métier des WebSockets de chat
    private final ChatWebSocketService chatWebSocketService;
    
    // Pattern pour extraire l'ID du canal depuis la destination WebSocket
    // Exemple: /chat/123 -> groupe 1 = 123
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("/chat/(\\d+)");
    private final UserService userService;

    /**
     * Gère l'événement de connexion d'un utilisateur
     * Se déclenche quand un client se connecte au WebSocket
     * 
     * @param event L'événement de connexion contenant les informations de session
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Récupère les en-têtes de la session WebSocket
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();
        
        
        if (destination != null) {
            // Extrait l'ID du canal depuis la destination
            // Exemple: si destination = "/chat/123", on extrait "123"
            Matcher matcher = CHANNEL_PATTERN.matcher(destination);
            if (matcher.find()) {
                Long channelId = Long.parseLong(matcher.group(1));
                // Récupère le nom d'utilisateur ou utilise "anonymous" si non disponible
                String username = headers.getUser() != null ? headers.getUser().getName() : "anonymous";
                Long userId = userService.getUserId(username);
                
                // Ajouter l'utilisateur au canal avec un ID temporaire
                chatWebSocketService.addUserToChannel(channelId, sessionId, username, userId);
            }
        }
    }

    /**
     * Gère l'événement de déconnexion d'un utilisateur
     * Se déclenche quand un client se déconnecte du WebSocket
     * 
     * @param event L'événement de déconnexion contenant les informations de session
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // Récupère les en-têtes de la session WebSocket
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        String destination = headers.getDestination();
        
        if (destination != null) {
            // Extrait l'ID du canal depuis la destination
            Matcher matcher = CHANNEL_PATTERN.matcher(destination);
            if (matcher.find()) {
                Long channelId = Long.parseLong(matcher.group(1));
                // Retire l'utilisateur du canal
                chatWebSocketService.removeUserFromChannel(channelId, sessionId);
            }
        }
    }
} 
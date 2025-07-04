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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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

    // Map sessionId -> ChannelUser
    private final Map<String, ChannelUser> sessionIdToChannelAndUser = new ConcurrentHashMap<>();

    // Petite classe utilitaire pour stocker channelId et userId
    public static class ChannelUser {
        public final Long channelId;
        public final Long userId;
        public ChannelUser(Long channelId, Long userId) {
            this.channelId = channelId;
            this.userId = userId;
        }
    }

    /**
     * Gère l'événement de connexion d'un utilisateur
     * Se déclenche quand un client se connecte au WebSocket
     * 
     * @param event L'événement de connexion contenant les informations de session
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        
        if (destination != null) {
            Matcher matcher = CHANNEL_PATTERN.matcher(destination);
            if (matcher.find()) {
                Long channelId = Long.parseLong(matcher.group(1));
                
                // Utiliser SecurityContextHolder (alimenté par JwtAuthenticationFilter)
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth != null ? auth.getName() : "anonymous";
                
                try {
                Long userId = userService.getUserId(username);
                    chatWebSocketService.addUserToChannel(channelId, userId);
                    System.out.println("Utilisateur connecté au canal " + channelId + ": " + username);
                } catch (IllegalArgumentException e) {
                    System.out.println("Erreur lors de l'ajout de l'utilisateur au canal: " + e.getMessage());
                }
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
        String sessionId = event.getSessionId();
        ChannelUser channelUser = sessionIdToChannelAndUser.remove(sessionId);
        if (channelUser != null) {
            chatWebSocketService.removeUserFromChannel(channelUser.channelId, channelUser.userId);
            System.out.println("Utilisateur déconnecté du canal " + channelUser.channelId + ": " + channelUser.userId);
        }
    }

    // Méthode à appeler lors du join (depuis le controller)
    public void registerSession(String sessionId, Long channelId, Long userId) {
        sessionIdToChannelAndUser.put(sessionId, new ChannelUser(channelId, userId));
    }
} 
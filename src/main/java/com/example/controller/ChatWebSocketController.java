package com.example.controller;

import com.example.service.ChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import com.example.service.UserService;
import com.example.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.event.WebSocketEventListener;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatWebSocketService chatWebSocketService;
    private final UserService userService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener webSocketEventListener;

    @MessageMapping("/chat/{channelId}/send")
    public void sendMessage(
            @DestinationVariable Long channelId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // Récupérer le token depuis les headers WebSocket
        String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
        String username = "anonymous";
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Décoder le JWT pour obtenir le username
                username = jwtService.extractUsername(token);
            } catch (Exception e) {
                System.out.println("Erreur lors du décodage du JWT: " + e.getMessage());
            }
        }
        
        // Envoyer le message au canal avec le sender
        chatWebSocketService.sendTextMessageToChannel(channelId, message, username);
    }

    @MessageMapping("/chat/{channelId}/join/{userId}")
    public void joinChannel(@DestinationVariable Long channelId, @DestinationVariable Long userId, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        webSocketEventListener.registerSession(sessionId, channelId, userId);
        chatWebSocketService.addUserToChannel(channelId, userId);
    }

    @PostMapping("/api/chat/{channelId}/file")
    @ResponseBody
    public void sendFile(
            @PathVariable Long channelId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        
        // Utiliser SecurityContextHolder (alimenté par JwtAuthenticationFilter)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        // Récupérer l'ID de l'utilisateur
        Long userId = userService.getUserId(username);
        
        // Envoyer le fichier au canal
        chatWebSocketService.sendFileToChannel(channelId, file, username);
    }

    // Retourner la liste des utilisateurs connectés au canal
    @GetMapping("/api/chat/{channelId}/users")
    public ResponseEntity<Map<String, String>> getChannelUsers(@PathVariable Long channelId) {
        return ResponseEntity.ok(chatWebSocketService.getChannelUsers(channelId));
    }

    // Cette méthode sera appelée automatiquement quand un utilisateur se déconnecte
    public void handleDisconnect(Long channelId, Long userId) {
        chatWebSocketService.removeUserFromChannel(channelId, userId);
    }

} 
package com.example.controller;

import com.example.service.ChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
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

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatWebSocketService chatWebSocketService;
    private final UserService userService;

    @MessageMapping("/chat/{channelId}/send")
    public void sendMessage(
            @DestinationVariable Long channelId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String sessionId = headerAccessor.getSessionId();
        
        // Ajouter l'utilisateur au canal s'il n'y est pas déjà
        chatWebSocketService.addUserToChannel(channelId, sessionId, username, 1L); // Temporairement userId = 1
        
        // Envoyer le message au canal
        chatWebSocketService.sendMessageToChannel(channelId, 
            String.format("%s: %s", username, message));
    }

    @SubscribeMapping("/chat/{channelId}")
    public Map<String, String> subscribeToChannel(
            @DestinationVariable Long channelId,
            SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";
        String sessionId = headerAccessor.getSessionId();
        
        // Ajouter l'utilisateur au canal
        chatWebSocketService.addUserToChannel(channelId, sessionId, username, 1L); // Temporairement userId = 1
        
        // Retourner la liste des utilisateurs connectés au canal
        return chatWebSocketService.getChannelUsers(channelId);
    }

    @PostMapping("/api/chat/{channelId}/file")
    @ResponseBody
    public void sendFile(
            @PathVariable Long channelId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {
        
        // Récupérer l'authentification depuis le contexte de sécurité à modifier du coup c'est pas la même que le headerAccessor
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String sessionId = request.getSession().getId();
        
        // Récupérer l'ID de l'utilisateur
        Long userId = userService.getUserId(username);
        
        // Ajouter l'utilisateur au canal s'il n'y est pas déjà
        chatWebSocketService.addUserToChannel(channelId, sessionId, username, userId);
        
        // Envoyer le fichier au canal
        chatWebSocketService.sendFileToChannel(channelId, file, username);
    }

    // Cette méthode sera appelée automatiquement quand un utilisateur se déconnecte
    public void handleDisconnect(Long channelId, String sessionId) {
        chatWebSocketService.removeUserFromChannel(channelId, sessionId);
    }
} 
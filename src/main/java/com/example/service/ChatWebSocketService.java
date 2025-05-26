package com.example.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import com.example.model.Channel;
import com.example.service.ChannelService;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.io.IOException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ChatWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelService channelService;
    
    // Map pour stocker les utilisateurs connectés par canal
    private final Map<Long, Map<String, String>> channelSubscriptions = new ConcurrentHashMap<>();

    // Types de fichiers autorisés
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of("application/pdf");
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB (verifier la taille max)

    public void sendMessageToChannel(Long channelId, String message) {
        String destination = "/topic/chat/" + channelId;
        messagingTemplate.convertAndSend(destination, message);
    }

    public void sendFileToChannel(Long channelId, MultipartFile file, String username) throws IOException {
        // Vérification du type de fichier
        String contentType = file.getContentType();
        if (!isFileTypeAllowed(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé");
        }

        // Vérification de la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 5MB)");
        }

        // Conversion du fichier en base64
        String base64File = Base64.getEncoder().encodeToString(file.getBytes());

        // Création du message avec le fichier
        Map<String, Object> fileMessage = new HashMap<>();
        fileMessage.put("type", "FILE");
        fileMessage.put("sender", username);
        fileMessage.put("fileName", file.getOriginalFilename());
        fileMessage.put("fileType", contentType);
        fileMessage.put("fileSize", file.getSize());
        fileMessage.put("content", base64File);
        fileMessage.put("timestamp", System.currentTimeMillis());

        // Envoi du message
        String destination = "/topic/chat/" + channelId;
        messagingTemplate.convertAndSend(destination, fileMessage);
    }

    private boolean isFileTypeAllowed(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType) || 
               ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    public void addUserToChannel(Long channelId, String sessionId, String username, Long userId) {
        // Vérification de l'existence du canal
        Channel channel = channelService.getChannelById(channelId)
            .orElseThrow(() -> new IllegalArgumentException("Channel pas trouvé"));

        // Vérification des droits d'accès
        List<Long> userIds = channelService.getChannelUserIds(channelId);
        if (!userIds.contains(userId)) {
            throw new IllegalArgumentException("L'utilisateur n'a pas été invité à ce salon");
        }

        // Si toutes les vérifications sont passées, on peut ajouter l'utilisateur
        Map<String, String> channelUsers = channelSubscriptions.computeIfAbsent(channelId, k -> new ConcurrentHashMap<>());
        channelUsers.put(sessionId, username);

        // Notification aux autres utilisateurs du canal
        messagingTemplate.convertAndSend(
            "/topic/chat/" + channelId,
            Map.of(
                "type", "USER_JOINED",
                "username", username,
                "connectedUsers", channelUsers
            )
        );
    }

    public void removeUserFromChannel(Long channelId, String sessionId) {
        Map<String, String> channelUsers = channelSubscriptions.get(channelId);
        if (channelUsers != null) {
            channelUsers.remove(sessionId);
            if (channelUsers.isEmpty()) {
                channelSubscriptions.remove(channelId);
            }
        }
    }

    public Map<String, String> getChannelUsers(Long channelId) {
        return channelSubscriptions.getOrDefault(channelId, new ConcurrentHashMap<>());
    }
} 
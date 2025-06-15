package com.example.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Set;
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
    
    // Map pour stocker les utilisateurs connectés par canal (username-based)
    private final Map<Long, Set<String>> channelSubscriptions = new ConcurrentHashMap<>();

    // Types de fichiers autorisés
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/gif");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of("application/pdf");
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public void sendTextMessageToChannel(Long channelId, String message, String username) {
        Map<String, Object> textMessage = new HashMap<>();
        textMessage.put("type", "TEXT");
        textMessage.put("sender", username);
        textMessage.put("content", message);
        textMessage.put("timestamp", System.currentTimeMillis());

        String destination = "/topic/chat/" + channelId;
        messagingTemplate.convertAndSend(destination, textMessage);
    }

    public void sendFileToChannel(Long channelId, MultipartFile file, String username) throws IOException {
        // Vérification du type de fichier
        String contentType = file.getContentType();
        if (!isFileTypeAllowed(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé");
        }

        // Vérification de la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 10MB)");
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

    public void addUserToChannel(Long channelId, Long userId) {
        // Vérification de l'existence du canal
        Channel channel = channelService.getChannelById(channelId)
            .orElseThrow(() -> new IllegalArgumentException("Channel pas trouvé"));

        // Vérification des droits d'accès
        List<Long> userIds = channelService.getChannelUserIds(channelId);
        if (!userIds.contains(userId) && !channel.getOwner().getUserId().equals(userId)) {
            throw new IllegalArgumentException("L'utilisateur n'a pas été invité à ce salon");
        }

        // Ajout de l'utilisateur
        Set<String> channelUsers = channelSubscriptions.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet());
        channelUsers.add(userId.toString());

        // Notification à tous les clients du canal (broadcast sur /topic)
        messagingTemplate.convertAndSend(
            "/topic/chat/" + channelId,
            Map.of(
                "type", "USER_JOINED",
                "userId", userId.toString(),
                "connectedUsers", channelUsers
            )
        );
    }

    public void removeUserFromChannel(Long channelId, Long userId) {
        Set<String> channelUsers = channelSubscriptions.get(channelId);
        if (channelUsers != null) {
            channelUsers.remove(userId.toString());
            if (channelUsers.isEmpty()) {
                channelSubscriptions.remove(channelId);
            }
        }

        // Notification à tous les clients du canal (broadcast sur /topic)
        messagingTemplate.convertAndSend(
            "/topic/chat/" + channelId,
            Map.of(
                "type", "USER_LEFT",
                "userId", userId.toString(),
                "connectedUsers", channelUsers != null ? channelUsers : Set.of()
            )
        );
    }

    public Map<String, String> getChannelUsers(Long channelId) {
        Set<String> users = channelSubscriptions.getOrDefault(channelId, ConcurrentHashMap.newKeySet());
        System.out.println("users: " + users);
        Map<String, String> result = new ConcurrentHashMap<>();
        users.forEach(userId -> result.put(userId, userId));
        return result;
    }
} 
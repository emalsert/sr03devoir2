package com.example.controller;

import com.example.model.Channel;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.service.JwtService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelRestController {
    
    @Autowired
    private ChannelService channelService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<Channel>> getUpcomingChannels() {
        return ResponseEntity.ok(channelService.getUpcomingChannels());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChannel(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int durationMinutes,
            @RequestParam Long ownerId) {
        try {
            /*on vérifie que la date est dans le futur
            System.out.println(date);
            System.out.println(LocalDateTime.now());
            if (date.isBefore(LocalDateTime.now().plusMinutes(10))) {
                return ResponseEntity.badRequest().body("La date doit être dans le futur");
            }*/
            Channel channel = channelService.createChannel(title, description, date, durationMinutes, ownerId);
            return ResponseEntity.ok("Canal créé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannel(@PathVariable Long id) {
        return channelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateChannel(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int durationMinutes,
            @RequestHeader("Authorization") String token) {
        try {
            //on vérifie que 'user est le même que celui du owner de channel
            String username = jwtService.extractUsername(token);
            if (username == null) {
                return ResponseEntity.badRequest().body("Pas owner");
            }
            
            Channel channel = channelService.updateChannel(id, title, description, date, durationMinutes);
            return ResponseEntity.ok(channel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteChannel(@PathVariable Long id) {
        try {
            channelService.deleteChannel(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 
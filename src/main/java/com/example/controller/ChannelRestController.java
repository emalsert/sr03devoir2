package com.example.controller;

import com.example.model.Channel;
import com.example.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "http://localhost:3000")
public class ChannelRestController {

    @Autowired
    private ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<Channel>> getUpcomingChannels() {
        return ResponseEntity.ok(channelService.getUpcomingChannels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannel(@PathVariable Long id) {
        return channelService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int duration,
            @RequestParam Long ownerId) {
        try {
            Channel channel = channelService.createChannel(title, description, date, duration, ownerId);
            return ResponseEntity.ok(channel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime date,
            @RequestParam int duration) {
        try {
            Channel channel = channelService.updateChannel(id, title, description, date, Duration.ofHours(duration));
            return ResponseEntity.ok(channel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long id) {
        try {
            channelService.deleteChannel(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 
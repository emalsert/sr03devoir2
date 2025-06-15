package com.example.controller;

import com.example.model.User;
import com.example.model.Channel;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/channels")
    public ResponseEntity<List<Channel>> getUserChannels(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserChannels(userId));
    }

    @GetMapping("/{userId}/channels/owner")
    public ResponseEntity<List<Channel>> getUserChannelsOwner(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserChannelsOwner(userId));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/edit")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(userId, user.getFirstName(), user.getLastName(), user.getEmail(), user.isAdmin(), user.getAvatar()));
    }
}
 
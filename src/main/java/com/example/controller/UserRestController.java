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
@CrossOrigin(origins = "http://localhost:3000")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/channels")
    public ResponseEntity<List<Channel>> getUserChannels(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserChannels(userId));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
 
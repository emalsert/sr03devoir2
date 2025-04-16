package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser.setAdmin(false);
        testUser.setConnected(false);
    }

    @Test
    void testCreateUser() {
        User savedUser = userService.createUser(testUser);
        assertNotNull(savedUser.getUserId());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
    }

    @Test
    void testGetUserById() {
        User savedUser = userService.createUser(testUser);
        Optional<User> foundUser = userService.getUserById(savedUser.getUserId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getUserId(), foundUser.get().getUserId());
    }

    @Test
    void testGetUserByEmail() {
        User savedUser = userService.createUser(testUser);
        Optional<User> foundUser = userService.getUserByEmail(savedUser.getEmail());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        userService.createUser(testUser);
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void testUpdateUser() {
        User savedUser = userService.createUser(testUser);
        savedUser.setFirstName("Jane");
        User updatedUser = userService.updateUser(savedUser);
        assertEquals("Jane", updatedUser.getFirstName());
    }

    @Test
    void testDeleteUser() {
        User savedUser = userService.createUser(testUser);
        userService.deleteUser(savedUser.getUserId());
        Optional<User> deletedUser = userService.getUserById(savedUser.getUserId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testExistsByEmail() {
        User savedUser = userService.createUser(testUser);
        assertTrue(userService.existsByEmail(savedUser.getEmail()));
        assertFalse(userService.existsByEmail("nonexistent@example.com"));
    }
} 
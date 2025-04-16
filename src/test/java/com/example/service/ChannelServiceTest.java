package com.example.service;

import com.example.model.Channel;
import com.example.model.User;
import com.example.repository.ChannelRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChannelServiceTest {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    private Channel testChannel;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password");
        testUser = userService.createUser(testUser);

        // Create test channel
        testChannel = new Channel();
        testChannel.setTitle("Test Channel");
        testChannel.setDescription("This is a test channel");
        testChannel.setDate(LocalDateTime.now().plusHours(1));
        testChannel.setDuration(Duration.ofHours(2));
        testChannel.setOwner(testUser);
    }

    @Test
    void testCreateChannel() {
        Channel savedChannel = channelService.createChannel(testChannel);
        assertNotNull(savedChannel.getChannelId());
        assertEquals("Test Channel", savedChannel.getTitle());
        assertEquals(testUser.getUserId(), savedChannel.getOwner().getUserId());
    }

    @Test
    void testGetChannelById() {
        Channel savedChannel = channelService.createChannel(testChannel);
        Optional<Channel> foundChannel = channelService.getChannelById(savedChannel.getChannelId());
        assertTrue(foundChannel.isPresent());
        assertEquals(savedChannel.getChannelId(), foundChannel.get().getChannelId());
    }

    @Test
    void testGetChannelsByOwner() {
        Channel savedChannel = channelService.createChannel(testChannel);
        List<Channel> channels = channelService.getChannelsByOwner(testUser);
        assertFalse(channels.isEmpty());
        assertEquals(savedChannel.getChannelId(), channels.get(0).getChannelId());
    }

    @Test
    void testGetUpcomingChannels() {
        Channel savedChannel = channelService.createChannel(testChannel);
        List<Channel> channels = channelService.getUpcomingChannels();
        assertFalse(channels.isEmpty());
        assertTrue(channels.stream().anyMatch(c -> c.getChannelId().equals(savedChannel.getChannelId())));
    }

    @Test
    void testGetChannelsBetween() {
        LocalDateTime now = LocalDateTime.now();
        Channel savedChannel = channelService.createChannel(testChannel);
        
        List<Channel> channels = channelService.getChannelsBetween(
            now.minusHours(1),
            now.plusHours(3)
        );
        
        assertFalse(channels.isEmpty());
        assertTrue(channels.stream().anyMatch(c -> c.getChannelId().equals(savedChannel.getChannelId())));
    }

    @Test
    void testUpdateChannel() {
        Channel savedChannel = channelService.createChannel(testChannel);
        savedChannel.setTitle("Updated Channel");
        Channel updatedChannel = channelService.updateChannel(savedChannel);
        assertEquals("Updated Channel", updatedChannel.getTitle());
    }

    @Test
    void testDeleteChannel() {
        Channel savedChannel = channelService.createChannel(testChannel);
        channelService.deleteChannel(savedChannel.getChannelId());
        Optional<Channel> deletedChannel = channelService.getChannelById(savedChannel.getChannelId());
        assertFalse(deletedChannel.isPresent());
    }
} 
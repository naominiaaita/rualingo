package com.example.rualingo.controller;

import com.example.rualingo.DTO.ChatLogDTO;
import com.example.rualingo.model.ChatLog;
import com.example.rualingo.model.ChatMessage;
import com.example.rualingo.repository.ChatLogRepository;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.service.ChatService;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatLogRepository chatLogRepository;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, ChatLogRepository chatLogRepository, UserRepository userRepository) {
        this.chatService = Objects.requireNonNull(chatService, "chatService must not be null");
        this.chatLogRepository = Objects.requireNonNull(chatLogRepository, "chatLogRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
    }

    @PostMapping("/ask")
    public ChatMessage askRua(@RequestBody ChatMessage userMessage, Authentication authentication) {
        Long authenticatedUserId = resolveAuthenticatedUserId(authentication);

        String ruaAnswer = chatService.processInput(userMessage.getUserQuery(), authenticatedUserId);
        userMessage.setResponse(ruaAnswer);

        var chatUser = authenticatedUserId != null ? userRepository.findById(authenticatedUserId).orElse(null) : null;
        ChatLog log = new ChatLog(chatUser, userMessage.getUserQuery(), ruaAnswer);
        chatLogRepository.save(log);
        ChatLogDTO.fromEntity(log); // keep controller decoupled via DTO mapping

        // Note: we intentionally do NOT trust userMessage.userId from the client.
        return userMessage;
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getMyAnalytics(Authentication authentication) {
        Long userId = resolveAuthenticatedUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(chatService.getUserAnalytics(userId));
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        var user = userRepository.findByEmail(username).orElse(null);
        if (user == null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
        return user != null ? user.getId() : null;
    }
}


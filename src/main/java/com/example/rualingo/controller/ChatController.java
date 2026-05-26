package com.example.rualingo.controller;

import com.example.rualingo.DTO.ChatTutorRequestDTO;
import com.example.rualingo.ai.AiTutorService;
import com.example.rualingo.model.ChatLog;
import com.example.rualingo.model.ChatMessage;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.ChatLogRepository;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.service.ChatService;
import java.util.List;
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
    private final AiTutorService aiTutorService;
    private final LanguageRepository languageRepository;
    private final LessonRepository lessonRepository;

    public ChatController(
            ChatService chatService,
            ChatLogRepository chatLogRepository,
            UserRepository userRepository,
            AiTutorService aiTutorService,
            LanguageRepository languageRepository,
            LessonRepository lessonRepository) {
        this.chatService = chatService;
        this.chatLogRepository = chatLogRepository;
        this.userRepository = userRepository;
        this.aiTutorService = aiTutorService;
        this.languageRepository = languageRepository;
        this.lessonRepository = lessonRepository;
    }

    @PostMapping("/ask")
    public ChatMessage askRua(@RequestBody ChatMessage userMessage, Authentication authentication) {
        if (userMessage == null) userMessage = new ChatMessage();
        String query = userMessage.getUserQuery() != null ? userMessage.getUserQuery() : "";
        
        Long authenticatedUserId = resolveAuthenticatedUserId(authentication);
        String ruaAnswer = chatService.processInput(query, authenticatedUserId);
        
        userMessage.setResponse(ruaAnswer);

        // Fail-safe logging
        try {
            final var finalId = authenticatedUserId;
            final var finalQuery = query;
            final var finalAnswer = ruaAnswer;
            var chatUser = finalId != null ? userRepository.findById(finalId).orElse(null) : null;
            chatLogRepository.save(new ChatLog(chatUser, finalQuery, finalAnswer));
        } catch (Exception ignored) {}

        return userMessage;
    }

    @PostMapping("/ask-v2")
    public ChatMessage askRuaV2(@RequestBody ChatTutorRequestDTO request, Authentication authentication) {
        Long authenticatedUserId = resolveAuthenticatedUserId(authentication);
        String username = "learner";
        
        var chatUser = authenticatedUserId != null ? userRepository.findById(authenticatedUserId).orElse(null) : null;
        if (chatUser != null) {
            if (chatUser.getFirstName() != null && !chatUser.getFirstName().isBlank()) {
                username = chatUser.getFirstName();
            } else if (chatUser.getUsername() != null && !chatUser.getUsername().isBlank()) {
                username = chatUser.getUsername();
            }
        }

        String message = (request != null && request.getMessage() != null) ? request.getMessage() : "";

        String ruaAnswer = "Rua says: I'm having a little trouble connecting. Try again?";
        try {
            if (aiTutorService.isEnabled()) {
                var language = request != null && request.getLanguageId() != null
                        ? languageRepository.findById(request.getLanguageId()).orElse(null)
                        : null;
                var lesson = request != null && request.getLessonId() != null
                        ? lessonRepository.findById(request.getLessonId()).orElse(null)
                        : null;
                List<ChatLog> history = authenticatedUserId != null
                        ? chatLogRepository.findTop20ByUser_IdOrderByTimestampDesc(authenticatedUserId)
                        : List.of();
                
                ruaAnswer = aiTutorService.reply(message, language, lesson, history, username);
            }
            
            if (ruaAnswer == null || ruaAnswer.isBlank()) {
                ruaAnswer = chatService.processInput(message, authenticatedUserId);
            }
        } catch (Exception e) {
            ruaAnswer = chatService.processInput(message, authenticatedUserId);
        }

        ChatMessage response = new ChatMessage();
        response.setUserQuery(message);
        response.setResponse(ruaAnswer);

        // Fail-safe logging (prevent 500 if DB fails)
        try {
            chatLogRepository.save(new ChatLog(chatUser, message, ruaAnswer));
        } catch (Exception ignored) {}

        return response;
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getMyAnalytics(Authentication authentication) {
        try {
            Long userId = resolveAuthenticatedUserId(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Unauthorized"));
            }
            return ResponseEntity.ok(chatService.getUserAnalytics(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    private Long resolveAuthenticatedUserId(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) return null;
            String name = authentication.getName();
            if (name == null || "anonymousUser".equalsIgnoreCase(name)) return null;
            
            return userRepository.findByEmail(name)
                    .or(() -> userRepository.findByUsername(name))
                    .map(com.example.rualingo.model.User::getId)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}

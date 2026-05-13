package com.example.rualingo.controller;

import com.example.rualingo.DTO.UserResponseDTO;
import com.example.rualingo.service.UserResponseService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user_responses")
public class UserResponseController {

    private final UserResponseService userResponseService;

    public UserResponseController(UserResponseService userResponseService) {
        this.userResponseService = userResponseService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUserResponse(@RequestBody UserResponseDTO request) {
        UserResponseDTO createdResponse = userResponseService.createUserResponse(
                request.getUserId(),
                request.getExerciseId(),
                request.getUserAnswer(),
                Boolean.TRUE.equals(request.getIsCorrect()),
                request.getAttempts() != null ? request.getAttempts() : 1);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUserResponses(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return userResponseService.getResponsesByUserId(userId);
        }
        return userResponseService.getAllUserResponses();
    }

    @GetMapping("/{responseId}")
    public UserResponseDTO getUserResponseById(@PathVariable Long responseId) {
        return userResponseService.getUserResponseById(responseId);
    }

    @DeleteMapping("/{responseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserResponse(@PathVariable Long responseId) {
        userResponseService.deleteUserResponse(responseId);
    }

    @GetMapping("/metrics/correct-count")
    public Map<String, Long> countCorrectResponses(@RequestParam Long userId) {
        return Map.of("correctResponses", userResponseService.countCorrectResponses(userId));
    }

    @GetMapping("/metrics/accuracy")
    public Map<String, Double> getAccuracy(@RequestParam Long userId) {
        return Map.of("accuracy", userResponseService.getAccuracy(userId));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}

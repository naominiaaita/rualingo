package com.example.rualingo.service;

import com.example.rualingo.DTO.UserResponseDTO;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.User;
import com.example.rualingo.model.UserResponse;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.UserRepository;
import com.example.rualingo.repository.UserResponseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserResponseService {

    private final UserResponseRepository userResponseRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public UserResponseService(
            UserResponseRepository userResponseRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository) {
        this.userResponseRepository = userResponseRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public UserResponseDTO createUserResponse(Long userId, Long exerciseId, String answer, boolean isCorrect, int attempts) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(requiredUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        Exercise exercise = exerciseRepository.findById(requiredExerciseId)
                .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setExercise(exercise);
        response.setAnswer(answer);
        response.setAttempts(attempts);
        response.setResponseTime(LocalDateTime.now());
        response.setTimeStamp(LocalDateTime.now());
        response.setIsCorrect(isCorrect);

        UserResponse savedResponse =
                Objects.requireNonNull(userResponseRepository.save(response), "Saved user response must not be null");
        return toDTO(savedResponse);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserResponseById(Long responseId) {
        UserResponse response = requireUserResponse(responseId);
        return toDTO(response);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUserResponses() {
        return userResponseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getResponsesByUserId(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userResponseRepository.findByUserId(requiredUserId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteUserResponse(Long responseId) {
        UserResponse response = requireUserResponse(responseId);
        userResponseRepository.delete(response);
    }

    @Transactional(readOnly = true)
    public long countCorrectResponses(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userResponseRepository.countCorrectByUserId(requiredUserId);
    }

    @Transactional(readOnly = true)
    public double getAccuracy(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        long total = userResponseRepository.countByUserId(requiredUserId);
        if (total == 0) {
            return 0.0;
        }
        return (userResponseRepository.countCorrectByUserId(requiredUserId) * 100.0) / total;
    }

    public UserResponseDTO toDTO(UserResponse response) {
        return new UserResponseDTO(
                response.getId(),
                response.getAnswer(),
                response.getIsCorrect(),
                response.getAttempts(),
                response.getResponseTime() != null ? response.getResponseTime().toString() : null,
                response.getTimeStamp() != null ? response.getTimeStamp().toString() : null,
                response.getUser() != null ? response.getUser().getId() : null,
                response.getExercise() != null ? response.getExercise().getId() : null);
    }

    private UserResponse requireUserResponse(Long responseId) {
        Long requiredResponseId = Objects.requireNonNull(responseId, "responseId must not be null");
        return userResponseRepository.findById(requiredResponseId)
                .orElseThrow(() -> new NoSuchElementException("User response not found: " + responseId));
    }
}

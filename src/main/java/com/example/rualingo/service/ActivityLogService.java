package com.example.rualingo.service;

import com.example.rualingo.DTO.ActivityLogDTO;
import com.example.rualingo.DTO.UserAnalyticsDTO;
import com.example.rualingo.model.ActivityLog;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.User;
import com.example.rualingo.repository.ActivityLogRepository;
import com.example.rualingo.repository.ChatLogRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

@Service
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final ChatLogRepository chatLogRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            UserRepository userRepository,
            LessonRepository lessonRepository,
            ExerciseRepository exerciseRepository,
            ChatLogRepository chatLogRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.chatLogRepository = chatLogRepository;
    }

    @Async("taskExecutor")
    public void createActivityLog(Long userId, String action, Long lessonId, Long exerciseId, Long clientTimestamp) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(requiredUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setAction(action);
        
        if (clientTimestamp != null) {
            activityLog.setTimestamp(java.time.Instant.ofEpochMilli(clientTimestamp)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        } else {
            activityLog.setTimestamp(java.time.LocalDateTime.now());
        }

        if (lessonId != null) {
            Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
            Lesson lesson = lessonRepository.findById(requiredLessonId)
                    .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + lessonId));
            activityLog.setLesson(lesson);
        }
        if (exerciseId != null) {
            Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
            Exercise exercise = exerciseRepository.findById(requiredExerciseId)
                    .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));
            activityLog.setExercise(exercise);
        }
        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public ActivityLogDTO getActivityLogById(Long activityLogId) {
        ActivityLog activityLog =
                Objects.requireNonNull(requireActivityLog(activityLogId), "Activity log must not be null");
        return toDTO(activityLog);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getAllActivityLogs() {
        return activityLogRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getActivityLogsByUserId(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        return activityLogRepository.findByUserIdOrderByTimestampDesc(requiredUserId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteActivityLog(Long activityLogId) {
        ActivityLog activityLog =
                Objects.requireNonNull(requireActivityLog(activityLogId), "Activity log must not be null");
        activityLogRepository.delete(activityLog);
    }

    public ActivityLogDTO toDTO(ActivityLog activityLog) {
        return new ActivityLogDTO(
                activityLog.getId(),
                activityLog.getAction(),
                activityLog.getTimestamp() != null ? activityLog.getTimestamp().toString() : null,
                activityLog.getLesson() != null ? activityLog.getLesson().getId() : null,
                activityLog.getExercise() != null ? activityLog.getExercise().getId() : null,
                activityLog.getUser() != null ? activityLog.getUser().getId() : null);
    }

    @Transactional(readOnly = true)
    public UserAnalyticsDTO getUserAnalytics(Long userId) {
        Long requiredUserId = Objects.requireNonNull(userId, "userId must not be null");
        // Validate user exists (gives a clear 404-style message)
        userRepository.findById(requiredUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        List<ActivityLog> logs = activityLogRepository.findByUserIdOrderByTimestampDesc(requiredUserId);

        Map<String, Long> countsByAction = logs.stream()
                .filter(log -> log.getAction() != null && !log.getAction().isBlank())
                .collect(Collectors.groupingBy(
                        ActivityLog::getAction,
                        LinkedHashMap::new,
                        Collectors.counting()));

        long distinctLessons = logs.stream()
                .map(ActivityLog::getLesson)
                .filter(Objects::nonNull)
                .map(Lesson::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long distinctExercises = logs.stream()
                .map(ActivityLog::getExercise)
                .filter(Objects::nonNull)
                .map(Exercise::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        String lastActiveAt = logs.isEmpty() || logs.get(0).getTimestamp() == null
                ? null
                : logs.get(0).getTimestamp().toString();

        long totalChats = chatLogRepository.countByUserId(requiredUserId);
        String lastChatAt = chatLogRepository.findTopByUserIdOrderByTimestampDesc(requiredUserId)
                .map(chat -> chat.getTimestamp() != null ? chat.getTimestamp().toString() : null)
                .orElse(null);

        return new UserAnalyticsDTO(
                requiredUserId,
                (long) logs.size(),
                lastActiveAt,
                distinctLessons,
                distinctExercises,
                countsByAction,
                totalChats,
                lastChatAt);
    }

    private ActivityLog requireActivityLog(Long activityLogId) {
        Long requiredActivityLogId = Objects.requireNonNull(activityLogId, "activityLogId must not be null");
        return activityLogRepository.findById(requiredActivityLogId)
                .orElseThrow(() -> new NoSuchElementException("Activity log not found: " + activityLogId));
    }
}

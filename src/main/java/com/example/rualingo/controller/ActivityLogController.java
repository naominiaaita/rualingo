package com.example.rualingo.controller;

import com.example.rualingo.DTO.ActivityLogDTO;
import com.example.rualingo.service.ActivityLogService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/activity_logs")@PreAuthorize("hasRole('ADMIN')")public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @PostMapping
    public ResponseEntity<ActivityLogDTO> createActivityLog(@RequestBody ActivityLogDTO request) {
        ActivityLogDTO createdLog = activityLogService.createActivityLog(
                request.getUserId(),
                request.getAction(),
                request.getLessonId(),
                request.getExerciseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
    }

    @GetMapping
    public List<ActivityLogDTO> getAllActivityLogs(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return activityLogService.getActivityLogsByUserId(userId);
        }
        return activityLogService.getAllActivityLogs();
    }

    @GetMapping("/{activityLogId}")
    public ActivityLogDTO getActivityLogById(@PathVariable Long activityLogId) {
        return activityLogService.getActivityLogById(activityLogId);
    }

    @DeleteMapping("/{activityLogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivityLog(@PathVariable Long activityLogId) {
        activityLogService.deleteActivityLog(activityLogId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}

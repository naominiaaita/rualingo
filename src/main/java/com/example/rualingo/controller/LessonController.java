package com.example.rualingo.controller;

import com.example.rualingo.DTO.ExerciseDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.service.LessonService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonDTO> createLesson(@RequestBody LessonDTO lessonDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(lessonDTO));
    }

    @GetMapping
    public List<LessonDTO> getAllLessons(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return lessonService.getLessonsByStatus(status);
        }
        if (courseId != null) {
            return lessonService.getLessonsByCourse(courseId);
        }
        return lessonService.getAllLessons();
    }

    @GetMapping("/{lessonId}")
    public LessonDTO getLessonById(@PathVariable Long lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public LessonDTO updateLesson(@PathVariable Long lessonId, @RequestBody LessonDTO lessonDTO) {
        return lessonService.updateLesson(lessonId, lessonDTO);
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
    }

    @PatchMapping("/{lessonId}/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public LessonDTO assignCourse(@PathVariable Long lessonId, @PathVariable Long courseId) {
        return lessonService.assignCourse(lessonId, courseId);
    }

    @PostMapping("/{lessonId}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public LessonDTO submitLesson(@PathVariable Long lessonId) {
        return lessonService.submitLesson(lessonId);
    }

    @PatchMapping("/{lessonId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public LessonDTO approveLesson(@PathVariable Long lessonId, @RequestBody ModerationRequest request) {
        return lessonService.approveLesson(lessonId, request != null ? request.moderationNote() : null);
    }

    @PatchMapping("/{lessonId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public LessonDTO rejectLesson(@PathVariable Long lessonId, @RequestBody ModerationRequest request) {
        return lessonService.rejectLesson(lessonId, request != null ? request.moderationNote() : null);
    }

    @GetMapping("/{lessonId}/exercises")
    public List<ExerciseDTO> getExercisesForLesson(@PathVariable Long lessonId) {
        return lessonService.getExercisesForLesson(lessonId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    public record ModerationRequest(String moderationNote) {}
}

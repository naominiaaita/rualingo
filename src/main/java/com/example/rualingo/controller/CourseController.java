package com.example.rualingo.controller;

import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.service.CourseService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(courseDTO));
    }

    @GetMapping
    public List<CourseDTO> getAllCourses(
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) String status) {
        if (status != null) {
            return courseService.getCoursesByStatus(status);
        }
        if (languageId != null) {
            return courseService.getCoursesByLanguage(languageId);
        }
        return courseService.getAllCourses();
    }

    @GetMapping("/{courseId}")
    public CourseDTO getCourseById(@PathVariable Long courseId) {
        return courseService.getCourseById(courseId);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDTO updateCourse(@PathVariable Long courseId, @RequestBody CourseDTO courseDTO) {
        return courseService.updateCourse(courseId, courseDTO);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
    }

    @PatchMapping("/{courseId}/language/{languageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDTO assignLanguage(@PathVariable Long courseId, @PathVariable Long languageId) {
        return courseService.assignLanguage(courseId, languageId);
    }

    @PostMapping("/{courseId}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDTO submitCourse(@PathVariable Long courseId) {
        return courseService.submitCourse(courseId);
    }

    @PatchMapping("/{courseId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDTO approveCourse(@PathVariable Long courseId, @RequestBody ModerationRequest request) {
        return courseService.approveCourse(courseId, request != null ? request.moderationNote() : null);
    }

    @PatchMapping("/{courseId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public CourseDTO rejectCourse(@PathVariable Long courseId, @RequestBody ModerationRequest request) {
        return courseService.rejectCourse(courseId, request != null ? request.moderationNote() : null);
    }

    @GetMapping("/{courseId}/lessons")
    public List<LessonDTO> getLessonsForCourse(@PathVariable Long courseId) {
        return courseService.getLessonsForCourse(courseId);
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

package com.example.rualingo.controller;

import com.example.rualingo.model.Course;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LessonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class LessonImportController {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;

    public LessonImportController(CourseRepository courseRepository, LessonRepository lessonRepository, ExerciseRepository exerciseRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @PostMapping("/lessons")
    public ResponseEntity<?> importLessons(@RequestBody List<Map<String, Object>> data) {
        // Simple implementation to import lessons and exercises from JSON
        for (Map<String, Object> item : data) {
            Long courseId = ((Number) item.get("courseId")).longValue();
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) continue;

            Lesson lesson = new Lesson();
            lesson.setTitle((String) item.get("title"));
            lesson.setDescription((String) item.get("description"));
            lesson.setContent((String) item.get("content"));
            lesson.setCourse(course);
            lesson.setSubmissionStatus("APPROVED");
            Lesson savedLesson = lessonRepository.save(lesson);

            List<Map<String, Object>> exercises = (List<Map<String, Object>>) item.get("exercises");
            if (exercises != null) {
                for (Map<String, Object> exData : exercises) {
                    Exercise exercise = new Exercise();
                    exercise.setLesson(savedLesson);
                    exercise.setType((String) exData.get("type"));
                    exercise.setQuestionText((String) exData.get("questionText"));
                    exercise.setCorrectAnswer((String) exData.get("correctAnswer"));
                    exercise.setOptions((String) exData.get("options"));
                    exerciseRepository.save(exercise);
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "Import successful"));
    }
}

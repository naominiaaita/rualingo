package com.example.rualingo.controller;

import com.example.rualingo.DTO.ExerciseDTO;
import com.example.rualingo.service.ExerciseService;
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
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExerciseDTO> createExercise(@RequestBody ExerciseDTO exerciseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.createExercise(exerciseDTO));
    }

    @GetMapping
    public List<ExerciseDTO> getAllExercises(@RequestParam(required = false) Long lessonId) {
        if (lessonId != null) {
            return exerciseService.getExercisesByLesson(lessonId);
        }
        return exerciseService.getAllExercises();
    }

    @GetMapping("/{exerciseId}")
    public ExerciseDTO getExerciseById(@PathVariable Long exerciseId) {
        return exerciseService.getExerciseById(exerciseId);
    }

    @PutMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciseDTO updateExercise(@PathVariable Long exerciseId, @RequestBody ExerciseDTO exerciseDTO) {
        return exerciseService.updateExercise(exerciseId, exerciseDTO);
    }

    @DeleteMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable Long exerciseId) {
        exerciseService.deleteExercise(exerciseId);
    }

    @PatchMapping("/{exerciseId}/lesson/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ExerciseDTO assignLesson(@PathVariable Long exerciseId, @PathVariable Long lessonId) {
        return exerciseService.assignLesson(exerciseId, lessonId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}

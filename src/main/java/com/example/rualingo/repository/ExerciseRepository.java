package com.example.rualingo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.Exercise;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByLessonId(Long lessonId);
}

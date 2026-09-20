package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Course;
import com.example.rualingo.model.Lesson;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long>{
    Optional<Lesson> findByTitleAndCourse(String title, Course course);
}

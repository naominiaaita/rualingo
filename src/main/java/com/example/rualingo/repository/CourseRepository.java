package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Course;
import com.example.rualingo.model.Language;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>{
    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findByNameContainingIgnoreCase(String name);
    Optional<Course> findByTitleAndLanguage(String title, Language language);
}

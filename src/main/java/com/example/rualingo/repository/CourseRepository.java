package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.rualingo.model.Course;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>{
    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findByNameContainingIgnoreCase(String name);
}

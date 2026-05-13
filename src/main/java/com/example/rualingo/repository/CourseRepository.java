package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>{

}

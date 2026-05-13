package com.example.rualingo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>{

}

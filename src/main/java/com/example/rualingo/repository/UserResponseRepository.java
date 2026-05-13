package com.example.rualingo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.rualingo.model.UserResponse;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
    @Query("SELECT ur FROM UserResponse ur WHERE ur.user.id = :userId")
    List<UserResponse> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ur) FROM UserResponse ur WHERE ur.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ur) FROM UserResponse ur WHERE ur.user.id = :userId AND ur.isCorrect = TRUE")
    long countCorrectByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(DISTINCT ur.exercise.id)
            FROM UserResponse ur
            WHERE ur.user.id = :userId
              AND ur.exercise.lesson.id = :lessonId
              AND ur.isCorrect = TRUE
            """)
    long countDistinctCorrectExercisesByUserIdAndLessonId(
            @Param("userId") Long userId,
            @Param("lessonId") Long lessonId);

    @Query("""
            SELECT ur
            FROM UserResponse ur
            WHERE ur.user.id = :userId
              AND ur.exercise.lesson.id = :lessonId
            ORDER BY ur.timeStamp DESC
            """)
    List<UserResponse> findByUserIdAndLessonIdOrderByTimeStampDesc(
            @Param("userId") Long userId,
            @Param("lessonId") Long lessonId);

    Optional<UserResponse> findTopByUserIdAndExerciseIdOrderByIdDesc(Long userId, Long exerciseId);
}

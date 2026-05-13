package com.example.rualingo.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.rualingo.model.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>{
    @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);

    List<ActivityLog> findByUserIdAndActionOrderByTimestampDesc(Long userId, String action);

    Optional<ActivityLog> findTopByUserIdAndLessonIdAndActionOrderByTimestampDesc(Long userId, Long lessonId, String action);
}

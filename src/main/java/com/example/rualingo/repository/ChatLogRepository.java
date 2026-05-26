package com.example.rualingo.repository;

import com.example.rualingo.model.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
   
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    long countByUser_Id(Long userId);

    Optional<ChatLog> findTopByUser_IdOrderByTimestampDesc(Long userId);

    List<ChatLog> findTop20ByUser_IdOrderByTimestampDesc(Long userId);
}


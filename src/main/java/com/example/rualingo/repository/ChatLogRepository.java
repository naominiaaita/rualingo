package com.example.rualingo.repository;

import com.example.rualingo.model.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
   
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
}




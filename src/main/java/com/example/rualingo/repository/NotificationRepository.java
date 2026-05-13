package com.example.rualingo.repository;

import com.example.rualingo.model.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);

    Optional<Notification> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}

package com.example.rualingo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification")
public class Notification {

    @Id
    @Column(name = "notification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Column(name = "message_text")
    private String message;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

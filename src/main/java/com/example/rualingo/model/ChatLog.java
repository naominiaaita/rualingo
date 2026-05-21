package com.example.rualingo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs", indexes = @Index(name = "idx_chat_logs_user_id", columnList = "user_id"))
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String userQuery;
    private String ruaResponse;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Default constructor (Required by JPA)
    public ChatLog() {}

    // Parameterized constructor
    public ChatLog(String userQuery, String ruaResponse) {
        this.userQuery = userQuery;
        this.ruaResponse = ruaResponse;
    }

    public ChatLog(User user, String userQuery, String ruaResponse) {
        this.user = user;
        this.userQuery = userQuery;
        this.ruaResponse = ruaResponse;
    }

    // Getters and Setters
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public String getRuaResponse() {
        return ruaResponse;
    }

    public void setRuaResponse(String ruaResponse) {
        this.ruaResponse = ruaResponse;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

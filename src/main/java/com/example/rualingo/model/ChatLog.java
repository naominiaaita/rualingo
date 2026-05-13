package com.example.rualingo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_logs")
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;
    
    private Long userId;
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

    // Getters and Setters
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

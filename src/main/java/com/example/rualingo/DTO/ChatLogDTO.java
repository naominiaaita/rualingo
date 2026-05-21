package com.example.rualingo.DTO;

import com.example.rualingo.model.ChatLog;

public class ChatLogDTO {
    private Long id;
    private Long userId;
    private String userQuery;
    private String ruaResponse;
    private String timestamp;

    public ChatLogDTO() {}

    public ChatLogDTO(Long id, Long userId, String userQuery, String ruaResponse, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.userQuery = userQuery;
        this.ruaResponse = ruaResponse;
        this.timestamp = timestamp;
    }

    public static ChatLogDTO fromEntity(ChatLog chatLog) {
        if (chatLog == null) {
            return null;
        }
        Long id = chatLog.getLogId();
        Long userId = chatLog.getUser() != null ? chatLog.getUser().getId() : null;
        String userQuery = chatLog.getUserQuery();
        String ruaResponse = chatLog.getRuaResponse();
        String timestamp = chatLog.getTimestamp() != null ? chatLog.getTimestamp().toString() : null;
        return new ChatLogDTO(id, userId, userQuery, ruaResponse, timestamp);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

package com.example.rualingo.model;

public class ChatMessage {

    private Long userId;      // Optional: who is chatting (for activity logs)
    private String userQuery; // What the user typed
    private String response;  // What Rua says back

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }



}

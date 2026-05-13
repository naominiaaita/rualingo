package com.example.rualingo.DTO;

public class NotificationDTO {
    private Long id;
    private String type;
    private String message;
    private boolean read;
    private String createdAt;
    private String scheduledFor;
    private String sentAt;

    public NotificationDTO() {}

    public NotificationDTO(
            Long id,
            String type,
            String message,
            boolean read,
            String createdAt,
            String scheduledFor,
            String sentAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.scheduledFor = scheduledFor;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(String scheduledFor) { this.scheduledFor = scheduledFor; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}

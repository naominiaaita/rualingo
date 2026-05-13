package com.example.rualingo.DTO;

public class ActivityLogDTO {
    private Long id;
    private String action;
    private String timestamp;
    private Long lessonId;
    private Long exerciseId;
    private Long userId;

    public ActivityLogDTO() {}

    public ActivityLogDTO(Long id, String action, String timestamp, Long lessonId, Long exerciseId, Long userId) {
        this.id = id;
        this.action = action;
        this.timestamp = timestamp;
        this.lessonId = lessonId;
        this.exerciseId = exerciseId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

package com.example.rualingo.DTO;

public class UserResponseDTO {
    private Long id;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer attempts;
    private String responseTime;
    private String timeStamp;
    private Long userId;
    private Long exerciseId;

    public UserResponseDTO() {}

    public UserResponseDTO(
            Long id,
            String userAnswer,
            Boolean isCorrect,
            Integer attempts,
            String responseTime,
            String timeStamp,
            Long userId,
            Long exerciseId) {
        this.id = id;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.attempts = attempts;
        this.responseTime = responseTime;
        this.timeStamp = timeStamp;
        this.userId = userId;
        this.exerciseId = exerciseId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public String getResponseTime() { return responseTime; }
    public void setResponseTime(String responseTime) { this.responseTime = responseTime; }

    public String getTimeStamp() { return timeStamp; }
    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
}

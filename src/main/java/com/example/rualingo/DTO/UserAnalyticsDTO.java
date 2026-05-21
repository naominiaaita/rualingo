package com.example.rualingo.DTO;

import java.util.Map;

public class UserAnalyticsDTO {
    private Long userId;
    private Long totalActivities;
    private String lastActiveAt;
    private Long distinctLessons;
    private Long distinctExercises;
    private Map<String, Long> activityCountsByAction;

    private Long totalChats;
    private String lastChatAt;

    public UserAnalyticsDTO() {}

    public UserAnalyticsDTO(
            Long userId,
            Long totalActivities,
            String lastActiveAt,
            Long distinctLessons,
            Long distinctExercises,
            Map<String, Long> activityCountsByAction,
            Long totalChats,
            String lastChatAt) {
        this.userId = userId;
        this.totalActivities = totalActivities;
        this.lastActiveAt = lastActiveAt;
        this.distinctLessons = distinctLessons;
        this.distinctExercises = distinctExercises;
        this.activityCountsByAction = activityCountsByAction;
        this.totalChats = totalChats;
        this.lastChatAt = lastChatAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTotalActivities() { return totalActivities; }
    public void setTotalActivities(Long totalActivities) { this.totalActivities = totalActivities; }

    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Long getDistinctLessons() { return distinctLessons; }
    public void setDistinctLessons(Long distinctLessons) { this.distinctLessons = distinctLessons; }

    public Long getDistinctExercises() { return distinctExercises; }
    public void setDistinctExercises(Long distinctExercises) { this.distinctExercises = distinctExercises; }

    public Map<String, Long> getActivityCountsByAction() { return activityCountsByAction; }
    public void setActivityCountsByAction(Map<String, Long> activityCountsByAction) {
        this.activityCountsByAction = activityCountsByAction;
    }

    public Long getTotalChats() { return totalChats; }
    public void setTotalChats(Long totalChats) { this.totalChats = totalChats; }

    public String getLastChatAt() { return lastChatAt; }
    public void setLastChatAt(String lastChatAt) { this.lastChatAt = lastChatAt; }
}


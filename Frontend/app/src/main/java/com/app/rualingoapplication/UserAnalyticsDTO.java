package com.app.rualingoapplication;

import java.util.List;
import java.util.Map;

public class UserAnalyticsDTO {
    private List<Map<String, Object>> activities;
    private List<ChatMessage> recentChats;
    private int totalActivities;
    private int totalChats;

    public List<Map<String, Object>> getActivities() {
        return activities;
    }

    public void setActivities(List<Map<String, Object>> activities) {
        this.activities = activities;
    }

    public List<ChatMessage> getRecentChats() {
        return recentChats;
    }

    public void setRecentChats(List<ChatMessage> recentChats) {
        this.recentChats = recentChats;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getTotalChats() {
        return totalChats;
    }

    public void setTotalChats(int totalChats) {
        this.totalChats = totalChats;
    }
}

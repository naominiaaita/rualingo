package com.example.rualingo.DTO;

public class ProgressStatsDTO {
    private int currentStreakDays;
    private int longestStreakDays;
    private int totalXp;
    private int completedLessons;
    private String lastCompletedAt;

    public ProgressStatsDTO() {}

    public ProgressStatsDTO(
            int currentStreakDays,
            int longestStreakDays,
            int totalXp,
            int completedLessons,
            String lastCompletedAt) {
        this.currentStreakDays = currentStreakDays;
        this.longestStreakDays = longestStreakDays;
        this.totalXp = totalXp;
        this.completedLessons = completedLessons;
        this.lastCompletedAt = lastCompletedAt;
    }

    public int getCurrentStreakDays() { return currentStreakDays; }
    public void setCurrentStreakDays(int currentStreakDays) { this.currentStreakDays = currentStreakDays; }

    public int getLongestStreakDays() { return longestStreakDays; }
    public void setLongestStreakDays(int longestStreakDays) { this.longestStreakDays = longestStreakDays; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }

    public String getLastCompletedAt() { return lastCompletedAt; }
    public void setLastCompletedAt(String lastCompletedAt) { this.lastCompletedAt = lastCompletedAt; }
}

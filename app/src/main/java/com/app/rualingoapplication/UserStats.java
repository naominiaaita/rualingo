package com.app.rualingoapplication;

import java.io.Serializable;

public class UserStats implements Serializable {
    private int totalXp;
    private int streak;
    private int lessonsCompleted;
    private int accuracyRate;

    public UserStats() {}

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getLessonsCompleted() { return lessonsCompleted; }
    public void setLessonsCompleted(int lessonsCompleted) { this.lessonsCompleted = lessonsCompleted; }

    public int getAccuracyRate() { return accuracyRate; }
    public void setAccuracyRate(int accuracyRate) { this.accuracyRate = accuracyRate; }
}

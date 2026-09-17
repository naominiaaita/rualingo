package com.app.rualingoapplication.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_logs")
public class PendingLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private Long userId;
    private String action;
    private Long lessonId;
    private Long exerciseId;
    private Long clientTimestamp;

    public PendingLog(Long userId, String action, Long lessonId, Long exerciseId, Long clientTimestamp) {
        this.userId = userId;
        this.action = action;
        this.lessonId = lessonId;
        this.exerciseId = exerciseId;
        this.clientTimestamp = clientTimestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public Long getLessonId() { return lessonId; }
    public Long getExerciseId() { return exerciseId; }
    public Long getClientTimestamp() { return clientTimestamp; }
}

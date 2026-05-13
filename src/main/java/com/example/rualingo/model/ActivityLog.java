package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @Column(name = "activity_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    @Column(name = "time_stamp")
    private LocalDateTime timestamp;
    
 //Relationships//
 
    @ManyToOne
    @JoinColumn(name = "lesson_id") 
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private User user;


    public ActivityLog() {}

    public ActivityLog(String action, LocalDateTime timestamp, User user) {
        this.action = action;
        this.timestamp = timestamp;
        this.user = user;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

     public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

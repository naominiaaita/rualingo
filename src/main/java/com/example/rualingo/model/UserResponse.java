package com.example.rualingo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_response")
public class UserResponse {

    @Id
    @Column(name = "userresponse_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_answer")
    private String userAnswer;
    @Column(name = "is_correct")
    private Boolean isCorrect;
    private Integer attempts;
    @Column(name = "response_time")
    private LocalDateTime responseTime;
    @Column(name = "time_stamp")
    private LocalDateTime timeStamp;

    
    // Relationships //
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    public UserResponse() {}

    public UserResponse(String userAnswer, Boolean correct, User user, Exercise exercise) {
        this.userAnswer = userAnswer;
        this.isCorrect = correct;
        this.user = user;
        this.exercise = exercise;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAnswer() { return userAnswer; }
    public void setAnswer(String response) { this.userAnswer = response; }


    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public LocalDateTime getTimeStamp() { return timeStamp; }
    public void setTimeStamp(LocalDateTime timeStamp) { this.timeStamp = timeStamp; }

    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

}

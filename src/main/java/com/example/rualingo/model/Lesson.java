package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @Column(name = "lesson_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String content;
    @Column(name = "submission_status")
    private String submissionStatus;
    @Column(name = "moderation_note")
    private String moderationNote;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "topic")
    private String topic;

//Relationships//

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

   @OneToMany(mappedBy = "lesson") 
   private Set<ActivityLog> activityLogs = new HashSet<>();

    public Lesson() {}

    public Lesson(String title, String description, Course course) {
        this.title = title;
        this.description = description;
        this.course = course;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Set<ActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(Set<ActivityLog> activityLogs) { this.activityLogs = activityLogs; }

      public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}

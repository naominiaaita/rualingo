package com.example.rualingo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LessonDTO {
    @JsonProperty("lesson_id")
    @JsonAlias("id")
    private Long id;
    
    @NotBlank(message = "Lesson title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;
    
    @NotBlank(message = "Lesson content is required")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;
    
    @NotNull(message = "Course ID is required")
    @JsonProperty("course_id")
    @JsonAlias("courseId")
    private Long courseId;
    
    @Pattern(regexp = "DRAFT|PENDING|APPROVED|REJECTED", message = "Invalid submission status")
    @JsonProperty("submission_status")
    @JsonAlias("submissionStatus")
    private String submissionStatus;
    
    @Size(max = 1000, message = "Moderation note must not exceed 1000 characters")
    @JsonProperty("moderation_note")
    @JsonAlias("moderationNote")
    private String moderationNote;
    
    @JsonProperty("reviewed_at")
    @JsonAlias("reviewedAt")
    private String reviewedAt;

    private String topic;

    public LessonDTO() {}

    public LessonDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public LessonDTO(Long id, String title, String description, String content, Long courseId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.courseId = courseId;
    }

    public LessonDTO(
            Long id,
            String title,
            String description,
            String content,
            Long courseId,
            String submissionStatus,
            String moderationNote,
            String reviewedAt,
            String topic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.courseId = courseId;
        this.submissionStatus = submissionStatus;
        this.moderationNote = moderationNote;
        this.reviewedAt = reviewedAt;
        this.topic = topic;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}

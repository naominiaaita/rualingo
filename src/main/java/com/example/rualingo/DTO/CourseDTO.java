package com.example.rualingo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseDTO {
    @JsonProperty("course_id")
    @JsonAlias("id")
    private Long id;
    
    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;
    
    @NotNull(message = "Language ID is required")
    @JsonProperty("language_id")
    @JsonAlias("languageId")
    private Long languageId;
    
    @Pattern(regexp = "Language|Professional|Academic", message = "Category must be Language, Professional, or Academic")
    private String category;
    
    private String metadata;
    
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

    public CourseDTO() {}

    public CourseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public CourseDTO(Long id, String title, String name, String description, Long languageId) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.description = description;
        this.languageId = languageId;
    }

    public CourseDTO(
            Long id,
            String title,
            String name,
            String description,
            Long languageId,
            String category,
            String metadata,
            String submissionStatus,
            String moderationNote,
            String reviewedAt) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.description = description;
        this.languageId = languageId;
        this.category = category;
        this.metadata = metadata;
        this.submissionStatus = submissionStatus;
        this.moderationNote = moderationNote;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
}

package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Course implements Serializable {
    @SerializedName(value = "course_id", alternate = {"id"})
    private Long id;

    private String title;
    private String description;

    @SerializedName("language_id")
    private Long languageId;

    @SerializedName("name")
    private String languageName;

    @SerializedName("moderation_note")
    private String moderationNote;

    @SerializedName("reviewed_at")
    private String reviewedAt;

    @SerializedName("submission_status")
    private String submissionStatus;

    private String flag;

    public Course() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    
    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
}

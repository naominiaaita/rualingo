package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Lesson implements Serializable {
    @SerializedName(value = "lesson_id", alternate = {"id", "lessonId"})
    private Long id;

    private String title;
    private String description;

    private String content;

    @SerializedName(value = "course_id", alternate = {"courseId"})
    private Long courseId;

    private String topic;

    @SerializedName("moderation_note")
    private String moderationNote;

    @SerializedName("submission_status")
    private String submissionStatus;

    @SerializedName(value = "reviewed_at", alternate = {"reviewedAt", "reviewedat"})
    private String reviewedAt;

    public Lesson() {}

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

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }
    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
}

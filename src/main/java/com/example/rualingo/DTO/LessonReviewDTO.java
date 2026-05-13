package com.example.rualingo.DTO;

import java.util.List;

public class LessonReviewDTO {
    private Long lessonId;
    private String title;
    private String description;
    private String content;
    private Long courseId;
    private boolean completed;
    private String completedAt;
    private List<LessonReviewExerciseDTO> exercises;

    public LessonReviewDTO() {}

    public LessonReviewDTO(
            Long lessonId,
            String title,
            String description,
            String content,
            Long courseId,
            boolean completed,
            String completedAt,
            List<LessonReviewExerciseDTO> exercises) {
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.courseId = courseId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.exercises = exercises;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public List<LessonReviewExerciseDTO> getExercises() { return exercises; }
    public void setExercises(List<LessonReviewExerciseDTO> exercises) { this.exercises = exercises; }
}

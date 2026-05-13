package com.example.rualingo.DTO;

public class CompletedLessonDTO {
    private Long lessonId;
    private String title;
    private Long courseId;
    private String completedAt;
    private int exerciseCount;
    private long correctExerciseCount;

    public CompletedLessonDTO() {}

    public CompletedLessonDTO(
            Long lessonId,
            String title,
            Long courseId,
            String completedAt,
            int exerciseCount,
            long correctExerciseCount) {
        this.lessonId = lessonId;
        this.title = title;
        this.courseId = courseId;
        this.completedAt = completedAt;
        this.exerciseCount = exerciseCount;
        this.correctExerciseCount = correctExerciseCount;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public int getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }

    public long getCorrectExerciseCount() { return correctExerciseCount; }
    public void setCorrectExerciseCount(long correctExerciseCount) { this.correctExerciseCount = correctExerciseCount; }
}

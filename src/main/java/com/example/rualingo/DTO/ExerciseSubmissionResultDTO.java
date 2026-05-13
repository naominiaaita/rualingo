package com.example.rualingo.DTO;

public class ExerciseSubmissionResultDTO {
    private Long exerciseId;
    private Long lessonId;
    private String answer;
    private boolean correct;
    private String feedbackMessage;
    private String suggestedHint;
    private int attempts;
    private long totalCorrectResponses;
    private double accuracy;

    public ExerciseSubmissionResultDTO() {}

    public ExerciseSubmissionResultDTO(
            Long exerciseId,
            Long lessonId,
            String answer,
            boolean correct,
            String feedbackMessage,
            String suggestedHint,
            int attempts,
            long totalCorrectResponses,
            double accuracy) {
        this.exerciseId = exerciseId;
        this.lessonId = lessonId;
        this.answer = answer;
        this.correct = correct;
        this.feedbackMessage = feedbackMessage;
        this.suggestedHint = suggestedHint;
        this.attempts = attempts;
        this.totalCorrectResponses = totalCorrectResponses;
        this.accuracy = accuracy;
    }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getFeedbackMessage() { return feedbackMessage; }
    public void setFeedbackMessage(String feedbackMessage) { this.feedbackMessage = feedbackMessage; }

    public String getSuggestedHint() { return suggestedHint; }
    public void setSuggestedHint(String suggestedHint) { this.suggestedHint = suggestedHint; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public long getTotalCorrectResponses() { return totalCorrectResponses; }
    public void setTotalCorrectResponses(long totalCorrectResponses) { this.totalCorrectResponses = totalCorrectResponses; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
}

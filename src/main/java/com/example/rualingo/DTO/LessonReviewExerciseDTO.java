package com.example.rualingo.DTO;

public class LessonReviewExerciseDTO {
    private Long exerciseId;
    private String type;
    private String questionText;
    private String question;
    private String options;
    private String hint;
    private String lastAnswer;
    private Boolean lastCorrect;
    private Integer attempts;
    private String responseTime;

    public LessonReviewExerciseDTO() {}

    public LessonReviewExerciseDTO(
            Long exerciseId,
            String type,
            String questionText,
            String question,
            String options,
            String hint,
            String lastAnswer,
            Boolean lastCorrect,
            Integer attempts,
            String responseTime) {
        this.exerciseId = exerciseId;
        this.type = type;
        this.questionText = questionText;
        this.question = question;
        this.options = options;
        this.hint = hint;
        this.lastAnswer = lastAnswer;
        this.lastCorrect = lastCorrect;
        this.attempts = attempts;
        this.responseTime = responseTime;
    }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getLastAnswer() { return lastAnswer; }
    public void setLastAnswer(String lastAnswer) { this.lastAnswer = lastAnswer; }

    public Boolean getLastCorrect() { return lastCorrect; }
    public void setLastCorrect(Boolean lastCorrect) { this.lastCorrect = lastCorrect; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public String getResponseTime() { return responseTime; }
    public void setResponseTime(String responseTime) { this.responseTime = responseTime; }
}

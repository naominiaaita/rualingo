package com.example.rualingo.DTO;

public class StudentExerciseDTO {
    private Long id;
    private String type;
    private String questionText;
    private String question;
    private String options;
    private String hint;
    private Long lessonId;

    public StudentExerciseDTO() {}

    public StudentExerciseDTO(
            Long id,
            String type,
            String questionText,
            String question,
            String options,
            String hint,
            Long lessonId) {
        this.id = id;
        this.type = type;
        this.questionText = questionText;
        this.question = question;
        this.options = options;
        this.hint = hint;
        this.lessonId = lessonId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
}

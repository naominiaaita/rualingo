package com.example.rualingo.DTO;

import java.util.List;

public class QuestionDTO {
    private Long id;
    private String prompt;
    private List<String> options;
    private String correctAnswer;

    public QuestionDTO() {}

    public QuestionDTO(Long id, String prompt, List<String> options, String correctAnswer) {
        this.id = id;
        this.prompt = prompt;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}

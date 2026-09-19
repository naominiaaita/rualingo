package com.example.rualingo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExerciseDTO {
    @JsonProperty("exercise_id")
    @JsonAlias("id")
    private Long id;
    
    @NotBlank(message = "Exercise type is required")
    @Pattern(regexp = "multiple_choice|short_answer|matching|true_false|TRANSLATION|translation|MULTIPLE_CHOICE", message = "Invalid exercise type")
    private String type;
    
    @JsonProperty("question_text")
    @JsonAlias("questionText")
    private String questionText;
    
    @NotBlank(message = "Question is required")
    private String question;
    
    @NotBlank(message = "Answer is required")
    @JsonProperty("correct_answer")
    @JsonAlias({"answer", "correctAnswer"})
    private String correctAnswer;
    
    private String options;
    
    @Size(max = 200, message = "Hint must not exceed 200 characters")
    private String hint;

    @JsonProperty("audio_path")
    @JsonAlias("audioPath")
    private String audioPath;

    private String topic;

    @NotNull(message = "Lesson ID is required")
    @JsonProperty("lesson_id")
    @JsonAlias("lessonId")
    private Long lessonId;

    private String lessonTitle;

    public ExerciseDTO() {}

    public ExerciseDTO(String question, String correctAnswer) {
        this.question = question;
        this.correctAnswer = correctAnswer;
    }

    public ExerciseDTO(
            Long id,
            String type,
            String questionText,
            String question,
            String correctAnswer,
            String options,
            String hint,
            String audioPath,
            String topic,
            Long lessonId) {
        this.id = id;
        this.type = type;
        this.questionText = questionText;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.hint = hint;
        this.audioPath = audioPath;
        this.topic = topic;
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

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
}

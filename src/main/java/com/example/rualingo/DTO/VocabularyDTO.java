package com.example.rualingo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyDTO {
    private Long id;
    
    @NotBlank(message = "Target word is required")
    @Size(min = 1, max = 100, message = "Target word must be between 1 and 100 characters")
    private String wordTarget;
    
    @NotBlank(message = "Word is required")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String word;
    
    @Size(min = 0, max = 200, message = "Phonetic must not exceed 200 characters")
    private String phonetic;
    
    @Size(min = 0, max = 500, message = "Example sentence must not exceed 500 characters")
    private String exampleSentence;
    
    @NotBlank(message = "Translation is required")
    @Size(min = 1, max = 200, message = "Translation must be between 1 and 200 characters")
    private String translation;
    
    @JsonProperty("language_id")
    @JsonAlias({"languageId", "languageID", "language"})
    @NotNull(message = "Language ID is required")
    private Long languageId;

    @JsonProperty("course_id")
    @JsonAlias({"courseId", "courseID", "course"})
    private Long courseId;

    @JsonProperty("lesson_id")
    @JsonAlias({"lessonId", "lessonID", "lesson"})
    private Long lessonId;

    private String topic;

    public VocabularyDTO() {}

    public VocabularyDTO(String word, String translation) {
        this.word = word;
        this.translation = translation;
    }

    public VocabularyDTO(
            Long id,
            String wordTarget,
            String word,
            String phonetic,
            String exampleSentence,
            String translation,
            Long languageId,
            Long courseId,
            Long lessonId,
            String topic) {
        this.id = id;
        this.wordTarget = wordTarget;
        this.word = word;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.translation = translation;
        this.languageId = languageId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.topic = topic;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWordTarget() { return wordTarget; }
    public void setWordTarget(String wordTarget) { this.wordTarget = wordTarget; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}

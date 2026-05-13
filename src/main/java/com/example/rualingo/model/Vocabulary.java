package com.example.rualingo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vocabulary")
public class Vocabulary {

    @Id
    @Column(name = "vocab_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_target")
    private String wordTarget;
    private String word;
    private String phonetic;
    @Column(name = "example_sentence")
    private String exampleSentence;
    private String translation;

    @Column(name = "topic")
    private String topic;
    
//Relationships//
    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    public Vocabulary() {}

    public Vocabulary(String word, String translation, Language language) {
        this.word = word;
        this.translation = translation;
        this.language = language;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getWordTarget() { return wordTarget; }
    public void setWordTarget(String wordTarget) { this.wordTarget = wordTarget; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
}
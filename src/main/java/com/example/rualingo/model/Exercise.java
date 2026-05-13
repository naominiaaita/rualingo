package com.example.rualingo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "exercise")
public class Exercise {

    @Id
    @Column(name = "exercise_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; 
    @Column(name = "question_text")
    private String questionText;
    private String question;
    @Column(name = "correct_answer")
    private String correctAnswer;
    private String hint;

    @Column(name = "audio_path")
    private String audioPath;

    @Column(name = "topic")
    private String topic;


    //Relationships//
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseOption> optionItems = new ArrayList<>();

    @OneToMany(mappedBy = "exercise")
    private Set<UserResponse> responses = new HashSet<>();

    @OneToMany(mappedBy = "exercise")
    private Set<ActivityLog> activityLogs = new HashSet<>();

   
    public Exercise() {}

    public Exercise(String type, String question, String correctAnswer, Lesson lesson) {
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.lesson = lesson;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Set<UserResponse> getResponses() { return responses; }
    public void setResponses(Set<UserResponse> responses) { this.responses = responses; }

    public Set<ActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(Set<ActivityLog> activityLogs) { this.activityLogs = activityLogs; }

    public List<ExerciseOption> getOptionItems() { return optionItems; }
    public void setOptionItems(List<ExerciseOption> optionItems) {
        this.optionItems = optionItems != null ? optionItems : new ArrayList<>();
    }

    public String getOptions() {
        if (optionItems == null || optionItems.isEmpty()) {
            return null;
        }
        return optionItems.stream()
                .sorted((a, b) -> Integer.compare(
                        a.getOptionOrder() != null ? a.getOptionOrder() : 0,
                        b.getOptionOrder() != null ? b.getOptionOrder() : 0))
                .map(ExerciseOption::getOptionText)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(","));
    }

    public void setOptions(String optionsCsv) {
        optionItems.clear();
        if (optionsCsv == null || optionsCsv.isBlank()) {
            return;
        }
        String[] parts = optionsCsv.split(",");
        int order = 1;
        for (String raw : parts) {
            String text = raw == null ? "" : raw.trim();
            if (text.isEmpty()) {
                continue;
            }
            optionItems.add(new ExerciseOption(this, order++, text));
        }
    }

        
    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    
}

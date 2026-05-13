package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Question implements Serializable {

    @SerializedName(value = "exercise_id", alternate = {"id"})
    private Long id;

    private String type;

    @SerializedName("question_text")
    private String questionText;

    @SerializedName("question")
    private String question;

    @SerializedName("answer")
    private String answer;

    @SerializedName("options")
    private String rawOptions;

    private String hint;

    @SerializedName("lesson_id")
    private Long lessonId;

    private String phonetic;

    @SerializedName("example_sentence")
    private String exampleSentence;

    private String category;
    
    @SerializedName("sub_type")
    private String subType;

    private String metadata;

    @SerializedName("audio_path")
    private String audioPath;

    private String topic;

    public Question() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getOptions() { return rawOptions; }
    public void setOptions(String options) { this.rawOptions = options; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getPrompt() { return question != null ? question : questionText; }
    public String getCorrectAnswer() { return answer; }
    
    public List<String> getOptionsList() {
        if (rawOptions == null || rawOptions.isEmpty()) return new java.util.ArrayList<>();
        
        String clean = rawOptions.trim();
        // Remove outer brackets if they exist
        if (clean.startsWith("[") && clean.endsWith("]")) {
            clean = clean.substring(1, clean.length() - 1);
        }
        
        // Remove all quotes
        clean = clean.replace("\"", "");
        
        // Split by comma, semicolon, or newline
        String[] parts = clean.split("[,;\\n\\r]");
        List<String> result = new java.util.ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }
}

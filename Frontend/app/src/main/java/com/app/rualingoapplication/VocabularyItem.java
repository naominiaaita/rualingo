package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Map;

public class VocabularyItem implements Serializable {

    @SerializedName(value = "vocab_id", alternate = {"id"})
    private Long id;

    @SerializedName(value = "word_target", alternate = {"wordTarget"})
    private String wordTarget;

    private String word;
    private String phonetic;

    @SerializedName(value = "example_sentence", alternate = {"exampleSentence"})
    private String exampleSentence;

    private String translation;

    private String topic;

    @SerializedName(value = "lesson_id", alternate = {"lessonId", "lesson"})
    private Object lessonData;

    @SerializedName(value = "language_id", alternate = {"languageId"})
    private Long languageId;

    @SerializedName(value = "course_id", alternate = {"courseId"})
    private Long courseId;

    @SerializedName(value = "lesson_title", alternate = {"lessonTitle"})
    private String lessonTitle;

    @SerializedName(value = "audio_path", alternate = {"audioPath"})
    private String audioPath;

    public VocabularyItem() {}

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

    public Long getLessonId() {
        if (lessonData == null) return null;
        if (lessonData instanceof Number) return ((Number) lessonData).longValue();
        if (lessonData instanceof String) {
            try {
                return Long.parseLong((String) lessonData);
            } catch (Exception e) {
                return null;
            }
        }
        if (lessonData instanceof Map<?, ?> map) {
            Object objId = map.get("lesson_id");
            if (objId == null) objId = map.get("id");
            if (objId == null) objId = map.get("lessonId");
            if (objId instanceof Number) return ((Number) objId).longValue();
        }
        return null;
    }

    public void setLessonId(Long id) {
        this.lessonData = id;
    }

    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}

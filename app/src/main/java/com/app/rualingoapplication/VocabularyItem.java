package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VocabularyItem implements Serializable {

    @SerializedName(value = "vocab_id", alternate = {"id"})
    private Long id;

    @SerializedName("word_target")
    private String wordTarget;

    private String word;
    private String phonetic;

    @SerializedName("example_sentence")
    private String exampleSentence;

    private String translation;

    @SerializedName(value = "course_id", alternate = {"courseId", "courseID", "course_Id", "course"})
    private com.google.gson.JsonElement courseData;

    @SerializedName(value = "language_id", alternate = {"languageId", "languageID", "language_Id", "language"})
    private com.google.gson.JsonElement languageData;

    private String topic;

    @SerializedName(value = "language_name", alternate = {"languageName"})
    private String languageName;

    @SerializedName(value = "course_title", alternate = {"courseTitle", "title"})
    private String courseTitle;

    @SerializedName(value = "lesson_id", alternate = {"lessonId", "lesson"})
    private com.google.gson.JsonElement lessonData;

    @SerializedName(value = "lesson_title", alternate = {"lessonTitle"})
    private String lessonTitle;

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

    public Long getCourseId() {
        return extractId(courseData, "course_id");
    }
    
    public void setCourseId(Long id) {
        this.courseData = new com.google.gson.JsonPrimitive(id);
    }

    public Long getLanguageId() {
        return extractId(languageData, "language_id");
    }
    
    public void setLanguageId(Long id) {
        this.languageData = new com.google.gson.JsonPrimitive(id);
    }

    public Long getLessonId() {
        return extractId(lessonData, "lesson_id");
    }

    public void setLessonId(Long id) {
        this.lessonData = new com.google.gson.JsonPrimitive(id);
    }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    private Long extractId(com.google.gson.JsonElement element, String idKey) {
        if (element == null || element.isJsonNull()) return null;
        if (element.isJsonPrimitive()) {
            try {
                return element.getAsLong();
            } catch (Exception e) {
                return null;
            }
        }
        if (element.isJsonObject()) {
            com.google.gson.JsonObject obj = element.getAsJsonObject();
            if (obj.has(idKey)) return obj.get(idKey).getAsLong();
            if (obj.has("id")) return obj.get("id").getAsLong();
            if (obj.has("language_id")) return obj.get("language_id").getAsLong();
            if (obj.has("course_id")) return obj.get("course_id").getAsLong();
            if (obj.has("lesson_id")) return obj.get("lesson_id").getAsLong();
        }
        return null;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
}

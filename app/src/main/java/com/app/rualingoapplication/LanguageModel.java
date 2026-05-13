package com.app.rualingoapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LanguageModel implements Serializable {
    @SerializedName(value = "language_id", alternate = {"id"})
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("language_name")
    private String languageName;
    
    private String province;
    private String district;
    private String clan;
    private String flag;
    private int lessonCount;
    private int exerciseCount;
    private int vocabCount;
    private int audioCoverage;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLanguageName() { return languageName; }
    public String getProvince() { return province; }
    public String getDistrict() { return district; }
    public String getClan() { return clan; }
    public String getFlag() { return flag; }
    public int getLessonCount() { return lessonCount; }
    public int getExerciseCount() { return exerciseCount; }
    public int getVocabCount() { return vocabCount; }
    public int getAudioCoverage() { return audioCoverage; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public void setProvince(String province) { this.province = province; }
    public void setDistrict(String district) { this.district = district; }
    public void setClan(String clan) { this.clan = clan; }
    public void setFlag(String flag) { this.flag = flag; }
    public void setLessonCount(int lessonCount) { this.lessonCount = lessonCount; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }
    public void setVocabCount(int vocabCount) { this.vocabCount = vocabCount; }
    public void setAudioCoverage(int audioCoverage) { this.audioCoverage = audioCoverage; }
}

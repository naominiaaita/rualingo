package com.example.rualingo.DTO;

public class TranslationResultDTO {
    private Long languageId;
    private String languageName;
    private String english;
    private String translated;

    public TranslationResultDTO() {}

    public TranslationResultDTO(Long languageId, String languageName, String english, String translated) {
        this.languageId = languageId;
        this.languageName = languageName;
        this.english = english;
        this.translated = translated;
    }

    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getTranslated() { return translated; }
    public void setTranslated(String translated) { this.translated = translated; }
}


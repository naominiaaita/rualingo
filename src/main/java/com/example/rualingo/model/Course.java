package com.example.rualingo.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    @Column(name = "submission_status")
    private String submissionStatus;
    @Column(name = "moderation_note", columnDefinition = "TEXT")
    private String moderationNote;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    //Relationships//

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CourseMetadataEntry> metadataEntries = new HashSet<>();

    @OneToMany(mappedBy = "course")
    private Set<Lesson> lessons = new HashSet<>();

    @ManyToMany
    @JoinTable(
    name = "course_has_user", 
    joinColumns = @JoinColumn(name = "course_course_id"),
    inverseJoinColumns = @JoinColumn(name = "user_user_id") 
)
private Set<User> users = new HashSet<>();


    public Course() {}

    public Course(String name, String description, Language language) {
        this.name = name;
        this.description = description;
        this.language = language;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Set<CourseMetadataEntry> getMetadataEntries() { return metadataEntries; }
    public void setMetadataEntries(Set<CourseMetadataEntry> metadataEntries) {
        this.metadataEntries = metadataEntries != null ? metadataEntries : new HashSet<>();
    }

    public String getMetadata() {
        if (metadataEntries == null || metadataEntries.isEmpty()) {
            return null;
        }
        Map<String, String> map = metadataEntries.stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .collect(Collectors.toMap(
                        CourseMetadataEntry::getKey,
                        e -> e.getValue() != null ? e.getValue() : "",
                        (a, b) -> a,
                        LinkedHashMap::new));
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception ex) {
            return null;
        }
    }

    public void setMetadata(String metadata) {
        metadataEntries.clear();
        if (metadata == null || metadata.isBlank()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> parsed = mapper.readValue(metadata, new TypeReference<>() {});
            for (var entry : parsed.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                metadataEntries.add(new CourseMetadataEntry(this, key, value != null ? String.valueOf(value) : null));
            }
        } catch (Exception ex) {
            metadataEntries.add(new CourseMetadataEntry(this, "raw", metadata));
        }
    }

    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }

    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public Set<Lesson> getLessons() { return lessons; }
    public void setLessons(Set<Lesson> lessons) { this.lessons = lessons; }

    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}

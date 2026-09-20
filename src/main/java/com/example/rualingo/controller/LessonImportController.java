package com.example.rualingo.controller;

import com.example.rualingo.model.Course;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Language;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.model.Vocabulary;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LanguageRepository;
import com.example.rualingo.repository.LessonRepository;
import com.example.rualingo.repository.VocabularyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class LessonImportController {

    private final LanguageRepository languageRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final VocabularyRepository vocabularyRepository;

    public LessonImportController(LanguageRepository languageRepository, 
                                  CourseRepository courseRepository, 
                                  LessonRepository lessonRepository, 
                                  ExerciseRepository exerciseRepository,
                                  VocabularyRepository vocabularyRepository) {
        this.languageRepository = languageRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.vocabularyRepository = vocabularyRepository;
    }

    @PostMapping("/full-curriculum")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> importFullCurriculum(@RequestBody List<Map<String, Object>> languages) {
        for (Map<String, Object> langData : languages) {
            String langName = (String) langData.get("name");
            Language language = languageRepository.findByNameContainingIgnoreCase(langName)
                    .stream().findFirst()
                    .orElseGet(() -> {
                        Language nl = new Language();
                        nl.setName(langName);
                        nl.setProvince((String) langData.get("province"));
                        return languageRepository.save(nl);
                    });

            List<Map<String, Object>> coursesData = (List<Map<String, Object>>) langData.get("courses");
            if (coursesData != null) {
                for (Map<String, Object> courseData : coursesData) {
                    String courseTitle = (String) courseData.get("title");
                    Course course = courseRepository.findByTitleContainingIgnoreCase(courseTitle)
                            .stream().findFirst()
                            .orElseGet(() -> {
                                Course nc = new Course();
                                nc.setTitle(courseTitle);
                                nc.setName(courseTitle); // Set name as well for consistency
                                nc.setDescription((String) courseData.get("description"));
                                nc.setLanguage(language);
                                nc.setSubmissionStatus("APPROVED");
                                return courseRepository.save(nc);
                            });

                    List<Map<String, Object>> lessonsData = (List<Map<String, Object>>) courseData.get("lessons");
                    if (lessonsData != null) {
                        for (Map<String, Object> lessonData : lessonsData) {
                            Lesson lesson = new Lesson();
                            lesson.setTitle((String) lessonData.get("title"));
                            lesson.setDescription((String) lessonData.get("description"));
                            lesson.setContent((String) lessonData.get("content"));
                            lesson.setTopic((String) lessonData.get("topic"));
                            lesson.setCourse(course);
                            lesson.setSubmissionStatus("APPROVED");
                            Lesson savedLesson = lessonRepository.save(lesson);

                            List<Map<String, Object>> exercisesData = (List<Map<String, Object>>) lessonData.get("exercises");
                            if (exercisesData != null) {
                                for (Map<String, Object> exData : exercisesData) {
                                    Exercise ex = new Exercise();
                                    ex.setLesson(savedLesson);
                                    ex.setType((String) exData.get("type"));
                                    ex.setQuestionText((String) exData.get("questionText"));
                                    ex.setQuestion((String) exData.get("question"));
                                    ex.setCorrectAnswer((String) exData.get("correctAnswer"));
                                    ex.setOptions((String) exData.get("options"));
                                    ex.setHint((String) exData.get("hint"));
                                    ex.setTopic((String) exData.get("topic"));
                                    exerciseRepository.save(ex);

                                    // Auto-populate the Vocabulary table for dictionary consistency
                                    if ("vocabulary".equalsIgnoreCase(ex.getType())) {
                                        Vocabulary vocab = new Vocabulary();
                                        vocab.setWord(ex.getQuestion()); // The foreign word
                                        vocab.setTranslation(ex.getCorrectAnswer()); // The translation
                                        vocab.setTopic(ex.getTopic());
                                        vocab.setLanguage(language);
                                        vocab.setCourse(course);
                                        vocab.setLesson(savedLesson);
                                        vocabularyRepository.save(vocab);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "Full curriculum imported successfully"));
    }
}

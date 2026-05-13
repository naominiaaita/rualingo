package com.example.rualingo.service;

import com.example.rualingo.model.UserResponse;
import com.example.rualingo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class ReportsService {

    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final UserResponseRepository userResponseRepository;
    public ReportsService(
            CourseRepository courseRepository,
            LanguageRepository languageRepository,
            LessonRepository lessonRepository,
            ExerciseRepository exerciseRepository,
            VocabularyRepository vocabularyRepository,
            UserRepository userRepository,
            UserResponseRepository userResponseRepository) {
        this.courseRepository = courseRepository;
        this.languageRepository = languageRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.userResponseRepository = userResponseRepository;
    }

    public Map<String, Object> generateSystemReport() {
        Map<String, Object> report = new HashMap<>();

        // Courses
        Map<String, Object> coursesReport = new HashMap<>();
        coursesReport.put("total", courseRepository.count());
        coursesReport.put("byStatus", courseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        course -> course.getSubmissionStatus() != null ? course.getSubmissionStatus() : "DRAFT",
                        Collectors.counting())));
        coursesReport.put("byLanguage", courseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        course -> course.getLanguage() != null ? course.getLanguage().getName() : "No Language",
                        Collectors.counting())));
        report.put("courses", coursesReport);

        // Languages
        Map<String, Object> languagesReport = new HashMap<>();
        languagesReport.put("total", languageRepository.count());
        languagesReport.put("details", languageRepository.findAll().stream()
                .map(lang -> Map.of(
                        "id", lang.getId(),
                        "name", lang.getName(),
                        "province", lang.getProvince() != null ? lang.getProvince() : "N/A",
                        "district", lang.getDistrict() != null ? lang.getDistrict() : "N/A",
                        "clan", lang.getClan() != null ? lang.getClan() : "N/A",
                        "coursesCount", lang.getCourses() != null ? lang.getCourses().size() : 0,
                        "vocabulariesCount", lang.getVocabularies() != null ? lang.getVocabularies().size() : 0))
                .collect(Collectors.toList()));
        report.put("languages", languagesReport);

        // Lessons
        Map<String, Object> lessonsReport = new HashMap<>();
        lessonsReport.put("total", lessonRepository.count());
        lessonsReport.put("byCourse", lessonRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        lesson -> lesson.getCourse() != null ? lesson.getCourse().getTitle() : "No Course",
                        Collectors.counting())));
        report.put("lessons", lessonsReport);

        // Exercises
        Map<String, Object> exercisesReport = new HashMap<>();
        exercisesReport.put("total", exerciseRepository.count());
        exercisesReport.put("byType", exerciseRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        exercise -> exercise.getType() != null ? exercise.getType() : "Unknown",
                        Collectors.counting())));
        report.put("exercises", exercisesReport);

        // Users
        Map<String, Object> usersReport = new HashMap<>();
        usersReport.put("total", userRepository.count());
        usersReport.put("active", userRepository.findAll().stream().filter(u -> u.isActive()).count());
        report.put("users", usersReport);

        // User Responses (aggregated stats)
        Map<String, Object> responsesReport = new HashMap<>();
        long totalResponses = userResponseRepository.count();
        long totalCorrect = userResponseRepository.findAll().stream()
                .filter(response -> response.getIsCorrect() != null && response.getIsCorrect())
                .count();
        responsesReport.put("total", totalResponses);
        responsesReport.put("totalCorrect", totalCorrect);
        responsesReport.put("overallAccuracy", totalResponses > 0 ? (double) totalCorrect / totalResponses * 100 : 0.0);
        report.put("userResponses", responsesReport);

        return report;
    }

    /**
     * Generate comprehensive user learning progress report
     */
    public Map<String, Object> generateUserProgressReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        
        List<UserResponse> userResponses = userResponseRepository.findAll().stream()
                .filter(ur -> ur.getUser() != null && ur.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        long totalAttempts = userResponses.size();
        long correctAnswers = userResponses.stream()
                .filter(ur -> ur.getIsCorrect() != null && ur.getIsCorrect())
                .count();
        
        report.put("userId", userId);
        report.put("totalExercisesAttempted", totalAttempts);
        report.put("correctAnswers", correctAnswers);
        report.put("overallAccuracy", totalAttempts > 0 ? (double) correctAnswers / totalAttempts * 100 : 0.0);
        
        // Group by lesson
        Map<String, Map<String, Object>> lessonStats = userResponses.stream()
                .collect(Collectors.groupingBy(
                        ur -> ur.getExercise() != null && ur.getExercise().getLesson() != null 
                            ? ur.getExercise().getLesson().getTitle() 
                            : "Unknown Lesson",
                        Collectors.teeing(
                                Collectors.counting(),
                                Collectors.summingLong(ur -> (ur.getIsCorrect() != null && ur.getIsCorrect()) ? 1L : 0L),
                                (total, correct) -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("totalAttempts", total);
                                    stats.put("correctAnswers", correct);
                                    stats.put("accuracy", total > 0 ? (double) correct / total * 100 : 0.0);
                                    return stats;
                                }
                        )
                ));
        
        report.put("lessonStatistics", lessonStats);
        return report;
    }

    /**
     * Generate course performance report with detailed metrics
     */
    public Map<String, Object> generateCoursePerformanceReport(Long courseId) {
        Map<String, Object> report = new HashMap<>();
        
        List<UserResponse> courseResponses = userResponseRepository.findAll().stream()
                .filter(ur -> ur.getExercise() != null && 
                             ur.getExercise().getLesson() != null && 
                             ur.getExercise().getLesson().getCourse() != null &&
                             ur.getExercise().getLesson().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());

        report.put("courseId", courseId);
        report.put("totalResponses", courseResponses.size());
        
        Map<String, Long> performanceByType = courseResponses.stream()
                .filter(ur -> ur.getExercise().getType() != null)
                .collect(Collectors.groupingBy(
                        ur -> ur.getExercise().getType(),
                        Collectors.counting()
                ));
        
        report.put("responsesByType", performanceByType);
        
        return report;
    }
}

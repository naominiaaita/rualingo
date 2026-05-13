package com.example.rualingo.service;

import com.example.rualingo.DTO.ExerciseDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LessonRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LessonService {
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final ExerciseRepository exerciseRepository;

    public LessonService(
            LessonRepository lessonRepository,
            CourseRepository courseRepository,
            ExerciseRepository exerciseRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public LessonDTO createLesson(LessonDTO dto) {
        Lesson savedLesson = lessonRepository.save(toEntity(dto));
        return toDTO(savedLesson);
    }

    @Transactional(readOnly = true)
    public LessonDTO getLessonById(Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        return toDTO(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByStatus(String submissionStatus) {
        String requiredStatus = normalizeStatus(submissionStatus);
        return lessonRepository.findAll().stream()
                .filter(lesson -> requiredStatus.equalsIgnoreCase(defaultStatus(lesson.getSubmissionStatus())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LessonDTO updateLesson(Long lessonId, LessonDTO dto) {
        Lesson lesson = requireLesson(lessonId);
        if (dto.getTitle() != null) {
            lesson.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            lesson.setDescription(dto.getDescription());
        }
        if (dto.getContent() != null) {
            lesson.setContent(dto.getContent());
        }
        if (dto.getSubmissionStatus() != null) {
            lesson.setSubmissionStatus(normalizeStatus(dto.getSubmissionStatus()));
        }
        if (dto.getModerationNote() != null) {
            lesson.setModerationNote(dto.getModerationNote());
        }
        Lesson savedLesson = lessonRepository.save(lesson);
        return toDTO(savedLesson);
    }

    public void deleteLesson(Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        lessonRepository.delete(lesson);
    }

    public LessonDTO assignCourse(Long lessonId, Long courseId) {
        Lesson lesson = requireLesson(lessonId);
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        Course course = courseRepository.findById(requiredCourseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId));
        lesson.setCourse(course);
        Lesson savedLesson = lessonRepository.save(lesson);
        return toDTO(savedLesson);
    }

    public LessonDTO submitLesson(Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        lesson.setSubmissionStatus(STATUS_SUBMITTED);
        lesson.setModerationNote(null);
        lesson.setReviewedAt(null);
        return toDTO(lessonRepository.save(lesson));
    }

    public LessonDTO approveLesson(Long lessonId, String moderationNote) {
        Lesson lesson = requireLesson(lessonId);
        lesson.setSubmissionStatus(STATUS_APPROVED);
        lesson.setModerationNote(moderationNote);
        lesson.setReviewedAt(LocalDateTime.now());
        return toDTO(lessonRepository.save(lesson));
    }

    public LessonDTO rejectLesson(Long lessonId, String moderationNote) {
        Lesson lesson = requireLesson(lessonId);
        lesson.setSubmissionStatus(STATUS_REJECTED);
        lesson.setModerationNote(moderationNote);
        lesson.setReviewedAt(LocalDateTime.now());
        return toDTO(lessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public List<ExerciseDTO> getExercisesForLesson(Long lessonId) {
        Lesson lesson = requireLesson(lessonId);
        return exerciseRepository.findByLessonId(lesson.getId()).stream()
                .map(exercise -> new ExerciseDTO(
                        exercise.getId(),
                        exercise.getType(),
                        exercise.getQuestionText(),
                        exercise.getQuestion(),
                        exercise.getCorrectAnswer(),
                        exercise.getOptions(),
                        exercise.getHint(),
                        exercise.getAudioPath(),
                        exercise.getTopic(),
                        exercise.getLesson() != null ? exercise.getLesson().getId() : null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByCourse(Long courseId) {
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        return courseRepository.findById(requiredCourseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId))
                .getLessons().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findEntityById(Long lessonId) {
        Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
        return lessonRepository.findById(requiredLessonId);
    }

    public LessonDTO toDTO(Lesson lesson) {
        return new LessonDTO(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getContent(),
                lesson.getCourse() != null ? lesson.getCourse().getId() : null,
                defaultStatus(lesson.getSubmissionStatus()),
                lesson.getModerationNote(),
                lesson.getReviewedAt() != null ? lesson.getReviewedAt().toString() : null,
                lesson.getTopic());
    }

    public Lesson toEntity(LessonDTO dto) {
        Lesson lesson = new Lesson();
        lesson.setTitle(dto.getTitle());
        lesson.setDescription(dto.getDescription());
        lesson.setContent(dto.getContent());
        lesson.setSubmissionStatus(defaultStatus(dto.getSubmissionStatus()));
        lesson.setModerationNote(dto.getModerationNote());
        return lesson;
    }

    private String defaultStatus(String status) {
        return status == null || status.isBlank() ? STATUS_DRAFT : status;
    }

    private String normalizeStatus(String status) {
        String requiredStatus = Objects.requireNonNull(status, "submissionStatus must not be null").trim().toUpperCase();
        if (!requiredStatus.equals(STATUS_DRAFT)
                && !requiredStatus.equals(STATUS_SUBMITTED)
                && !requiredStatus.equals(STATUS_APPROVED)
                && !requiredStatus.equals(STATUS_REJECTED)) {
            throw new IllegalArgumentException("Unsupported submission status: " + status);
        }
        return requiredStatus;
    }

    private Lesson requireLesson(Long lessonId) {
    Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
    return lessonRepository.findById(requiredLessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + lessonId));
}

}

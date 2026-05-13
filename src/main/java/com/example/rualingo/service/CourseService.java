package com.example.rualingo.service;

import com.example.rualingo.DTO.CourseDTO;
import com.example.rualingo.DTO.LessonDTO;
import com.example.rualingo.model.Course;
import com.example.rualingo.model.Language;
import com.example.rualingo.repository.CourseRepository;
import com.example.rualingo.repository.LanguageRepository;
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
public class CourseService {
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;

    public CourseService(CourseRepository courseRepository, LanguageRepository languageRepository) {
        this.courseRepository = courseRepository;
        this.languageRepository = languageRepository;
    }

    public CourseDTO createCourse(CourseDTO dto) {
        Course savedCourse = courseRepository.save(toEntity(dto));
        return toDTO(savedCourse);
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long courseId) {
        Course course = requireCourse(courseId);
        return toDTO(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByStatus(String submissionStatus) {
        String requiredStatus = normalizeStatus(submissionStatus);
        return courseRepository.findAll().stream()
                .filter(course -> requiredStatus.equalsIgnoreCase(defaultStatus(course.getSubmissionStatus())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO updateCourse(Long courseId, CourseDTO dto) {
        Course course = requireCourse(courseId);
        if (dto.getTitle() != null) {
            course.setTitle(dto.getTitle());
        }
        if (dto.getName() != null) {
            course.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            course.setCategory(dto.getCategory());
        }
        if (dto.getMetadata() != null) {
            course.setMetadata(dto.getMetadata());
        }
        Course savedCourse = courseRepository.save(course);
        return toDTO(savedCourse);
    }

    public void deleteCourse(Long courseId) {
        Course course = requireCourse(courseId);
        courseRepository.delete(course);
    }

    public CourseDTO assignLanguage(Long courseId, Long languageId) {
        Course course = requireCourse(courseId);
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        Language language = languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId));
        course.setLanguage(language);
        Course savedCourse = courseRepository.save(course);
        return toDTO(savedCourse);
    }

    public CourseDTO submitCourse(Long courseId) {
        Course course = requireCourse(courseId);
        course.setSubmissionStatus(STATUS_SUBMITTED);
        course.setModerationNote(null);
        course.setReviewedAt(null);
        return toDTO(courseRepository.save(course));
    }

    public CourseDTO approveCourse(Long courseId, String moderationNote) {
        Course course = requireCourse(courseId);
        course.setSubmissionStatus(STATUS_APPROVED);
        course.setModerationNote(moderationNote);
        course.setReviewedAt(LocalDateTime.now());
        return toDTO(courseRepository.save(course));
    }

    public CourseDTO rejectCourse(Long courseId, String moderationNote) {
        Course course = requireCourse(courseId);
        course.setSubmissionStatus(STATUS_REJECTED);
        course.setModerationNote(moderationNote);
        course.setReviewedAt(LocalDateTime.now());
        return toDTO(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsForCourse(Long courseId) {
        return requireCourse(courseId).getLessons().stream()
                .map(lesson -> new LessonDTO(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getDescription(),
                        lesson.getContent(),
                        lesson.getCourse() != null ? lesson.getCourse().getId() : null,
                        defaultStatus(lesson.getSubmissionStatus()),
                        lesson.getModerationNote(),
                        lesson.getReviewedAt() != null ? lesson.getReviewedAt().toString() : null,
                        lesson.getTopic()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByLanguage(Long languageId) {
        Long requiredLanguageId = Objects.requireNonNull(languageId, "languageId must not be null");
        return languageRepository.findById(requiredLanguageId)
                .orElseThrow(() -> new NoSuchElementException("Language not found: " + languageId))
                .getCourses().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Course> findEntityById(Long courseId) {
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        return courseRepository.findById(requiredCourseId);
    }

    public CourseDTO toDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTitle(),
                course.getName(),
                course.getDescription(),
                course.getLanguage() != null ? course.getLanguage().getId() : null,
                course.getCategory(),
                course.getMetadata(),
                defaultStatus(course.getSubmissionStatus()),
                course.getModerationNote(),
                course.getReviewedAt() != null ? course.getReviewedAt().toString() : null);
    }

    public Course toEntity(CourseDTO dto) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());
        course.setMetadata(dto.getMetadata());
        course.setSubmissionStatus(defaultStatus(dto.getSubmissionStatus()));
        course.setModerationNote(dto.getModerationNote());
        return course;
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

    private Course requireCourse(Long courseId) {
        Long requiredCourseId = Objects.requireNonNull(courseId, "courseId must not be null");
        return courseRepository.findById(requiredCourseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found: " + courseId));
    }
}

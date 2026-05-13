package com.example.rualingo.service;

import com.example.rualingo.DTO.ExerciseDTO;
import com.example.rualingo.model.Exercise;
import com.example.rualingo.model.Lesson;
import com.example.rualingo.repository.ExerciseRepository;
import com.example.rualingo.repository.LessonRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;

    public ExerciseService(ExerciseRepository exerciseRepository, LessonRepository lessonRepository) {
        this.exerciseRepository = exerciseRepository;
        this.lessonRepository = lessonRepository;
    }

    public ExerciseDTO createExercise(ExerciseDTO dto) {
        Objects.requireNonNull(dto, "exerciseDTO must not be null");
        Exercise exercise = new Exercise();
        applyDto(exercise, dto);
        Exercise saved = exerciseRepository.save(exercise);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ExerciseDTO> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExerciseDTO getExerciseById(Long exerciseId) {
        Exercise exercise = requireExercise(exerciseId);
        return toDTO(exercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseDTO> getExercisesByLesson(Long lessonId) {
        Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
        return exerciseRepository.findByLessonId(requiredLessonId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExerciseDTO updateExercise(Long exerciseId, ExerciseDTO dto) {
        Objects.requireNonNull(dto, "exerciseDTO must not be null");
        Exercise exercise = requireExercise(exerciseId);
        applyDto(exercise, dto);
        Exercise saved = exerciseRepository.save(exercise);
        return toDTO(saved);
    }

    public void deleteExercise(Long exerciseId) {
        Exercise exercise = requireExercise(exerciseId);
        exerciseRepository.delete(exercise);
    }

    public ExerciseDTO assignLesson(Long exerciseId, Long lessonId) {
        Exercise exercise = requireExercise(exerciseId);
        Lesson lesson = requireLesson(lessonId);
        exercise.setLesson(lesson);
        Exercise saved = exerciseRepository.save(exercise);
        return toDTO(saved);
    }

    private Exercise requireExercise(Long exerciseId) {
        Long requiredExerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        return exerciseRepository.findById(requiredExerciseId)
                .orElseThrow(() -> new NoSuchElementException("Exercise not found: " + exerciseId));
    }

    private Lesson requireLesson(Long lessonId) {
        Long requiredLessonId = Objects.requireNonNull(lessonId, "lessonId must not be null");
        return lessonRepository.findById(requiredLessonId)
                .orElseThrow(() -> new NoSuchElementException("Lesson not found: " + lessonId));
    }

    private void applyDto(Exercise exercise, ExerciseDTO dto) {
        if (dto.getType() != null) {
            exercise.setType(dto.getType());
        }
        if (dto.getQuestionText() != null) {
            exercise.setQuestionText(dto.getQuestionText());
        }
        if (dto.getQuestion() != null) {
            exercise.setQuestion(dto.getQuestion());
        }
        if (dto.getAnswer() != null) {
            exercise.setCorrectAnswer(dto.getAnswer());
        }
        if (dto.getOptions() != null) {
            exercise.setOptions(dto.getOptions());
        }
        if (dto.getHint() != null) {
            exercise.setHint(dto.getHint());
        }
        if (dto.getAudioPath() != null) {
            exercise.setAudioPath(dto.getAudioPath());
        }
        if (dto.getTopic() != null) {
            exercise.setTopic(dto.getTopic());
        }
        if (dto.getLessonId() != null) {
            exercise.setLesson(requireLesson(dto.getLessonId()));
        }
    }

    private ExerciseDTO toDTO(Exercise exercise) {
        return new ExerciseDTO(
                exercise.getId(),
                exercise.getType(),
                exercise.getQuestionText(),
                exercise.getQuestion(),
                exercise.getCorrectAnswer(),
                exercise.getOptions(),
                exercise.getHint(),
                exercise.getAudioPath(),
                exercise.getTopic(),
                exercise.getLesson() != null ? exercise.getLesson().getId() : null);
    }
}

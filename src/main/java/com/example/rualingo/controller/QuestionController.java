package com.example.rualingo.controller;

import com.example.rualingo.DTO.ExerciseDTO;
import com.example.rualingo.DTO.QuestionDTO;
import com.example.rualingo.service.ExerciseService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final ExerciseService exerciseService;

    public QuestionController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<QuestionDTO> getQuestions() {
        return exerciseService.getAllExercises().stream().map(this::toQuestionDto).collect(Collectors.toList());
    }

    private QuestionDTO toQuestionDto(ExerciseDTO exerciseDTO) {
        return new QuestionDTO(
                exerciseDTO.getId(),
                choosePrompt(exerciseDTO),
                parseOptions(exerciseDTO.getOptions()),
                exerciseDTO.getAnswer());
    }

    private String choosePrompt(ExerciseDTO exerciseDTO) {
        if (exerciseDTO.getQuestionText() != null && !exerciseDTO.getQuestionText().isBlank()) {
            return exerciseDTO.getQuestionText();
        }
        return exerciseDTO.getQuestion();
    }

    private List<String> parseOptions(String options) {
        if (options == null || options.isBlank()) {
            return List.of();
        }
        return Arrays.stream(options.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

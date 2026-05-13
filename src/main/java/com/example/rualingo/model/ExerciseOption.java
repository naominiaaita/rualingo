package com.example.rualingo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercise_option")
public class ExerciseOption {

    @Id
    @Column(name = "exercise_option_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    public ExerciseOption() {}

    public ExerciseOption(Exercise exercise, Integer optionOrder, String optionText) {
        this.exercise = exercise;
        this.optionOrder = optionOrder;
        this.optionText = optionText;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public Integer getOptionOrder() { return optionOrder; }
    public void setOptionOrder(Integer optionOrder) { this.optionOrder = optionOrder; }

    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
}


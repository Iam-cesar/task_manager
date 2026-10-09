package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LabelIdsDto(
    @NotEmpty(message = "The labels list should not be empty.")
    List<Integer> label_ids
) {}

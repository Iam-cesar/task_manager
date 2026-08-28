package com.example.task_manager.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record LabelRecordsDto(
        @NotBlank
        @Max(60)
        String name,

        @NotBlank
        String color
) {};

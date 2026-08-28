package com.example.task_manager.dtos;

import com.example.task_manager.enums.ProjectStatusEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record ProjectRecordDto(
        @NotBlank
        @Max(60)
        String name,

        @Max(255)
        String description,

        ProjectStatusEnum projectStatus,

        Long userId
) {};

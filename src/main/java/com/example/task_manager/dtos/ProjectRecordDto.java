package com.example.task_manager.dtos;

import com.example.task_manager.enums.ProjectStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRecordDto(
        @NotBlank
        @Size(max = 60)
        String name,

        @Size(max = 255)
        String description,

        @NotNull
        ProjectStatusEnum projectStatus,

        @NotNull
        Long userId
) {}

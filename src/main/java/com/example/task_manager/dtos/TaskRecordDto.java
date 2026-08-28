package com.example.task_manager.dtos;

import com.example.task_manager.enums.TaskPriorityEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record TaskRecordDto(
        @NotBlank
        @Max(60)
        String title,

        @Max(255)
        String description,

        Long tagId,

        Long userId,

        Long projectId,

        TaskRecordDto taskStatus,

        TaskPriorityEnum taskPriority
) {};

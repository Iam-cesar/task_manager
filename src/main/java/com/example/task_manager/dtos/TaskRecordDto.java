package com.example.task_manager.dtos;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record TaskRecordDto(
        @NotBlank
        @Size(max = 60)
        String title,

        @Size(max = 255)
        String description,

        Set<Long> labelIds,

        @NotNull
        Long userId,

        @NotNull
        Long projectId,

        @NotNull
        TaskStatusEnum taskStatus,

        @NotNull
        TaskPriorityEnum taskPriority
) {}

package com.example.task_manager.dtos.input;

import com.example.task_manager.enums.TaskStatusEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusDto(@NotNull TaskStatusEnum status) {
}

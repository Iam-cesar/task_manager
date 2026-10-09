package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.TaskStatusEnum;

public record TaskStatusCountDto(TaskStatusEnum status, long count) {
}

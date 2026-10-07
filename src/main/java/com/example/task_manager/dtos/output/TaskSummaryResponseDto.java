package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.models.TaskModel;
import org.jspecify.annotations.NonNull;

public record TaskSummaryResponseDto(
	Integer id,
	String title,
	TaskStatusEnum status
) {

    public TaskSummaryResponseDto(final @NonNull TaskModel task) {
        this(task.getId(), task.getTitle(), task.getStatus());
    }
}

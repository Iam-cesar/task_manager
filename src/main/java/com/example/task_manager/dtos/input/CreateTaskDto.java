package com.example.task_manager.dtos.input;

import com.example.task_manager.models.UserModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTaskDto(
	@NotBlank(message = "Title should not be empty")
	@Size(min = 3, max = 255)
	String title,

	@Size(max = 255)
	String description,

	@NotNull
	int project_id,

	@NotNull
	int user_id,

	List<Integer> label_ids
) {
}

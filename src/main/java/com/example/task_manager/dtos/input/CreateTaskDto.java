package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record CreateTaskDto(
	@NotBlank(message = "Title should not be empty")
	@Size(min = 3, max = 60)
	String title,

	@Size(max = 255)
	String description,

	@NotNull
	int project_id,

	@NotNull
	int user_id,

	List<Integer> label_ids,

	@Schema(description = "Optional task deadline; must be a future date and time.", example = "2030-01-15T17:00:00Z")
	@Future(message = "Due date must be in the future")
	Instant due_date
) {
	public CreateTaskDto(
		String title,
		String description,
		int project_id,
		int user_id,
		List<Integer> label_ids
	) {
		this(title, description, project_id, user_id, label_ids, null);
	}
}

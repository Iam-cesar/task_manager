package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTaskDto(
	@NotBlank
	@Size(min = 3, max = 60)
	String title,

	@Size(max = 255)
	String description
) {
}

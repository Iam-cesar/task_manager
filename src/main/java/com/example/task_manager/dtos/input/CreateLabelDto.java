package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLabelDto(
	@NotBlank
	@Size(min = 3, max = 60)
	String name,

	@NotBlank
	@Size(min = 4, max = 20)
	String color
) {
}

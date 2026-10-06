package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLabelDto(
	@NotBlank
	@Size(max = 60)
	String name,

	@NotBlank
	@Size(max = 20)
	String color
) {
}

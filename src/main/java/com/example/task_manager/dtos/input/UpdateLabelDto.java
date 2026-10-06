package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLabelDto (
	@NotBlank
	@Size(max = 60)
	String name,

	@Size(max = 20)
	@NotBlank
	String color
) {

}

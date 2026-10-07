package com.example.task_manager.dtos.input;

import com.example.task_manager.models.UserModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.BeanUtils;

public record CreateUserDto(
	@NotBlank
	@Size(max = 60, min = 3)
	String name,

	@NotBlank
	@Email
	String email
) {
}

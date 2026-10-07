package com.example.task_manager.dtos.input;

import com.example.task_manager.models.UserModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.BeanUtils;

import com.example.task_manager.models.ProjectModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record CreateProjectDto(

	@NotBlank
	@Size(min = 3, max = 60)
	 String name,

	@Size(max = 255)
	String description,

	@NotNull
	Integer project_owner_id
) {
}

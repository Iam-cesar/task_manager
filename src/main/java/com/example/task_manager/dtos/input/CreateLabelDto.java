package com.example.task_manager.dtos.input;

import com.example.task_manager.models.LabelModel;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.BeanUtils;

public class CreateLabelDto {

	private String name;

	private String color;

	public CreateLabelDto(LabelModel aLabelModel) { BeanUtils.copyProperties(aLabelModel, this);	}
}

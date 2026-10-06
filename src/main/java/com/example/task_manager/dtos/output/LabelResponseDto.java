package com.example.task_manager.dtos.output;

import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.TaskModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LabelResponseDto extends RepresentationModel<LabelResponseDto> {
	private String name;

	private String color;

	private Set<TaskModel> tasks;

	public LabelResponseDto(LabelModel aLabel) {
		BeanUtils.copyProperties(aLabel, this);
	}
}

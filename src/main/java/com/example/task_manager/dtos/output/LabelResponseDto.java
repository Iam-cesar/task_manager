package com.example.task_manager.dtos.output;

import com.example.task_manager.models.LabelModel;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LabelResponseDto extends RepresentationModel<LabelResponseDto> {
	private Integer id;

	private String name;

	private String color;

	private Set<TaskSummaryResponseDto> tasks;

	private Instant created_at;

	public LabelResponseDto(LabelModel aLabel) {
		this(aLabel, true);
	}

	public LabelResponseDto(LabelModel aLabel, boolean includeTasks) {

		BeanUtils.copyProperties(aLabel, this, "tasks");

		this.tasks = includeTasks
			? aLabel.getTasks().stream()
				.map(TaskSummaryResponseDto::new)
				.collect(Collectors.toSet())
			: Set.of();
	}
}

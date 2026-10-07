package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskResponseDto extends RepresentationModel<TaskResponseDto> {

    private Integer id;

    private String title;

    private String description;

    private ProjectResponseDto project;

    private UserResponseDto user;

    private TaskStatusEnum status;

    private TaskPriorityEnum priority;

    private Instant due_date;

    private Instant completion_date;

    private boolean archived;

    private List<LabelResponseDto> labels;

    public TaskResponseDto(TaskModel taskModel) {

		BeanUtils.copyProperties(taskModel, this, "user", "project", "labels");

		this.user = new UserResponseDto(taskModel.getUser());

		this.project = new ProjectResponseDto(taskModel.getProject());

		this.labels = taskModel.getLabels().stream()
			.map(label -> new LabelResponseDto(label, false))
			.toList();

	}

}

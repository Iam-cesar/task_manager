package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.dtos.input.LabelIdsDto;
import com.example.task_manager.dtos.input.UpdateTaskDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.exceptions.TaskNotFoundException;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.repositories.LabelRepository;
import com.example.task_manager.repositories.TaskRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
	private final UserService userService;
	private final ProjectService projectService;
	private final LabelRepository labelRepository;

	@Transactional
	public TaskModel saveAndFlush(@NonNull CreateTaskDto aDto) {

		var user = userService.findById(aDto.user_id());
		var project = projectService.findById(aDto.project_id());
		var requestedLabelIds = aDto.label_ids() == null ? List.<Integer>of() : aDto.label_ids();
		var labels = new HashSet<>(labelRepository.findAllById(requestedLabelIds));
		if (labels.size() != new HashSet<>(requestedLabelIds).size()) {
			throw new LabelNotFoundException();
		}

		var task = new TaskModel(aDto, project, user, labels);

		var savedTask = taskRepository.saveAndFlush(task);
		return taskRepository.findWithRelationsById(savedTask.getId())
			.orElseThrow(TaskNotFoundException::new);
	}

	public TaskModel findById(final int anId) throws TaskNotFoundException {

		return taskRepository.findWithRelationsById(anId)
			.orElseThrow(TaskNotFoundException::new);
	}

	@Transactional(readOnly = true)
	public Page<TaskModel> findAll(final String search, Pageable pageable) {

		var idsPage =  search == null || search.isBlank()
			? taskRepository.findAllIds(pageable)
			: taskRepository.searchIdsByTitleAndDescription(search, pageable);


		if (idsPage.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, idsPage.getTotalElements());
		}

		var tasksById = taskRepository.findAllWithRelationsByIdIn(idsPage.getContent())
			.stream()
			.collect(Collectors.toMap(TaskModel::getId, Function.identity()));

		var projectsInPageOrder = idsPage.getContent().stream()
			.map(tasksById::get)
			.filter(Objects::nonNull)
			.toList();

		return new PageImpl<>(projectsInPageOrder, pageable, idsPage.getTotalElements());
	}

	@Transactional
	public TaskModel updateAndFlush(final int anId, @NonNull final UpdateTaskDto aDto) {

		var task = findById(anId);

		BeanUtils.copyProperties(aDto, task, "id");

		return taskRepository.saveAndFlush(task);
	}

	@Transactional
	public TaskModel addLabels(@NonNull LabelIdsDto aDto, Integer anTargetId)  {
		var targetTask = findById(anTargetId);
		var aListOfLabels = new HashSet<>(labelRepository.findAllById(aDto.label_ids()));

		for (LabelModel label : aListOfLabels) {
			var taskContainsThisLabel = targetTask.getLabels().contains(label);

			if (!taskContainsThisLabel) targetTask.addLabel(label);
		}

		return taskRepository.saveAndFlush(targetTask);
	}

	@Transactional
	public TaskModel removeLabels(@NonNull LabelIdsDto aDto, Integer anTargetId)  {
		var targetTask = findById(anTargetId);
		var aListOfLabels = new HashSet<>(labelRepository.findAllById(aDto.label_ids()));

		for (LabelModel label : aListOfLabels) {

			var taskContainsThisLabel = targetTask.getLabels().contains(label);

			if (taskContainsThisLabel) targetTask.removeLabel(label);
		}

		return taskRepository.saveAndFlush(targetTask);
	}


	public void delete(final int anId) {

		var task = findById(anId);

		if (task != null) {
			taskRepository.deleteById(task.getId());
		}
	}
}

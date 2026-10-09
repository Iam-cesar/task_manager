package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.dtos.input.LabelIdsDto;
import com.example.task_manager.dtos.input.UpdateTaskDto;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.exceptions.TaskDueDateInPastException;
import com.example.task_manager.exceptions.TaskNotFoundException;
import com.example.task_manager.dtos.output.TaskStatusCountDto;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

		if (aDto.due_date() != null && aDto.due_date().isBefore(java.time.Instant.now())) {
			throw new TaskDueDateInPastException();
		}

		final var user = userService.findById(aDto.user_id());
		final var project = projectService.findById(aDto.project_id());
		final var requestedLabelIds = aDto.label_ids() == null ? List.<Integer>of() : aDto.label_ids();
		final var labels = new HashSet<>(labelRepository.findAllById(requestedLabelIds));

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
		return findAll(search, false, pageable);
	}

	@Transactional(readOnly = true)
	public Page<TaskModel> findAll(final String search, boolean overdue, Pageable pageable) {

		boolean hasSearch = search != null && !search.isBlank();

		Page<Number> idsPage;

		if (overdue) {
			idsPage = hasSearch
				? taskRepository.searchOverdueIdsByTitleAndDescription(search, pageable)
				: taskRepository.findOverdueIds(pageable);
		} else {
			idsPage = hasSearch
				? taskRepository.searchIdsByTitleAndDescription(search, pageable)
				: taskRepository.findAllIds(pageable);
		}

		var integerIdsPage = idsPage.map(id -> Math.toIntExact(id.longValue()));

		if (integerIdsPage.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, integerIdsPage.getTotalElements());
		}

		var tasksById = taskRepository.findAllWithRelationsByIdIn(integerIdsPage.getContent())
			.stream()
			.collect(Collectors.toMap(TaskModel::getId, Function.identity()));

		var projectsInPageOrder = integerIdsPage.getContent().stream()
			.map(tasksById::get)
			.filter(Objects::nonNull)
			.toList();

		return new PageImpl<>(projectsInPageOrder, pageable, integerIdsPage.getTotalElements());
	}

	@Transactional
	public TaskModel updateAndFlush(final int anId, @NonNull final UpdateTaskDto aDto) {

		var task = findById(anId);

		BeanUtils.copyProperties(aDto, task, "id");

		return taskRepository.saveAndFlush(task);
	}

	@Transactional
	public TaskModel changeStatus(int anId, TaskStatusEnum status) {
		var task = findById(anId);
		task.changeStatus(status);
		return taskRepository.saveAndFlush(task);
	}

	@Transactional
	public TaskModel archive(int anId) {
		var task = findById(anId);
		task.archive();
		return taskRepository.saveAndFlush(task);
	}

	@Transactional(readOnly = true)
	public List<TaskStatusCountDto> summarizeByStatus() {
		final var counts = new EnumMap<>(TaskStatusEnum.class);

		for (TaskStatusEnum status : TaskStatusEnum.values()) {
			counts.put(status, 0L);
		}

		for (var row : taskRepository.countNonArchivedTasksByStatus()) {
			counts.put(row.getStatus(), row.getCount());
		}

		return counts.entrySet().stream()
			.map(entry -> new TaskStatusCountDto(entry.getKey(), (Long) entry.getValue()))
			.toList();
	}

	@Transactional
	public TaskModel addLabels(@NonNull LabelIdsDto aDto, Integer anTargetId)  {
		final var targetTask = findById(anTargetId);

		targetTask.ensureEditable();

		final var aListOfLabels = new HashSet<>(labelRepository.findAllById(aDto.label_ids()));

		for (LabelModel label : aListOfLabels) {
			final var taskContainsThisLabel = targetTask.getLabels().contains(label);

			if (!taskContainsThisLabel) targetTask.addLabel(label);
		}

		return taskRepository.saveAndFlush(targetTask);
	}

	@Transactional
	public TaskModel removeLabels(@NonNull LabelIdsDto aDto, Integer anTargetId)  {
		final var targetTask = findById(anTargetId);

		targetTask.ensureEditable();

		final var aListOfLabels = new HashSet<>(labelRepository.findAllById(aDto.label_ids()));

		for (LabelModel label : aListOfLabels) {

			final var taskContainsThisLabel = targetTask.getLabels().contains(label);

			if (taskContainsThisLabel) targetTask.removeLabel(label);
		}

		return taskRepository.saveAndFlush(targetTask);
	}


	public void delete(final int anId) {

		final var task = findById(anId);
		task.ensureEditable();
		taskRepository.deleteById(task.getId());
	}
}

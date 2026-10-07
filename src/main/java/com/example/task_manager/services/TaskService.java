package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.dtos.input.UpdateTaskDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.exceptions.TaskNotFoundException;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.repositories.LabelRepository;
import com.example.task_manager.repositories.TaskRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

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

    public List<TaskModel> searchByTitleAndDescription(@NonNull final String search) {

        List<Integer> taskIds = taskRepository.searchIdsByTitleAndDescription(search);

        if (taskIds.isEmpty()) {
            return List.of();
        }

        return taskRepository.findAllWithRelationsByIdIn(taskIds);
    }

	public TaskModel findById(final int anId) throws TaskNotFoundException {

		return taskRepository.findWithRelationsById(anId)
			.orElseThrow(TaskNotFoundException::new);
	}

	public List<TaskModel> findAll(final String search) {

		return search == null || search.isBlank()
			? taskRepository.findAllWithRelations()
			: searchByTitleAndDescription(search);
	}

	@Transactional
	public TaskModel updateAndFlush(final int anId, @NonNull final UpdateTaskDto aDto) {

		var task = findById(anId);

		BeanUtils.copyProperties(aDto, task, "id");

		return taskRepository.saveAndFlush(task);
	}

	public void delete(final int anId) {

		var task = findById(anId);

		if (task != null) {
			taskRepository.deleteById(task.getId());
		}
	}
}

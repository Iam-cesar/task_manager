package com.example.task_manager.controllers;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.dtos.input.LabelIdsDto;
import com.example.task_manager.dtos.input.UpdateTaskDto;
import com.example.task_manager.dtos.input.UpdateTaskStatusDto;
import com.example.task_manager.dtos.output.AllTasksResponseDto;
import com.example.task_manager.dtos.output.TaskResponseDto;
import com.example.task_manager.dtos.output.TaskStatusCountDto;
import com.example.task_manager.helpers.ConvertRepresentationModel;
import com.example.task_manager.services.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/tasks")
@AllArgsConstructor
public class TaskController {

	private final TaskService taskService;

	@PostMapping
	public ResponseEntity<TaskResponseDto> createTask(
		@RequestBody @Valid @NonNull final CreateTaskDto aDto,
		@NonNull final UriComponentsBuilder uriBuilder
	) {
		final var createdTask = taskService.saveAndFlush(aDto);

		URI uri = uriBuilder.path("/tasks/{id}")
			.buildAndExpand(createdTask.getId()).toUri();

		return ResponseEntity.created(uri).body(new TaskResponseDto(createdTask));
	}

	@GetMapping
	public ResponseEntity<PagedModel<AllTasksResponseDto>> getAllTasks(
		@RequestParam(required = false) String search,
		@RequestParam(defaultValue = "false") boolean overdue,
	    @PageableDefault(size = 10) final Pageable pageable
	) {
		final var tasks = ConvertRepresentationModel
			.toPageList(taskService.findAll(search, overdue, pageable), AllTasksResponseDto::new);

		if (!tasks.isEmpty()) {
			for (AllTasksResponseDto task : tasks.getContent()) {
				int id  = task.getId();
				task.add(linkTo(methodOn(TaskController.class).getTaskById(id)).withSelfRel());
			}
		}

		return ResponseEntity.ok(new PagedModel<>(tasks));
	}

	@GetMapping("/summary/status")
	public ResponseEntity<List<TaskStatusCountDto>> getStatusSummary() {
		return ResponseEntity.ok(taskService.summarizeByStatus());
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable final int id) {

		final var task = taskService.findById(id);

		return ResponseEntity.ok(new TaskResponseDto(task));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<TaskResponseDto> updateTaskById(
		@PathVariable final int id,
		@RequestBody @Valid @NonNull final UpdateTaskDto aDto
	) {
		final var updatedTasks = taskService.updateAndFlush(id, aDto);

		return ResponseEntity.ok(new TaskResponseDto(updatedTasks));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<TaskResponseDto> updateTaskStatus(
		@PathVariable final int id,
		@RequestBody @Valid @NonNull final UpdateTaskStatusDto aDto
	) {
		return ResponseEntity.ok(new TaskResponseDto(taskService.changeStatus(id, aDto.status())));
	}

	@PostMapping("/{id}/archive")
	public ResponseEntity<TaskResponseDto> archiveTask(@PathVariable final int id) {
		return ResponseEntity.ok(new TaskResponseDto(taskService.archive(id)));
	}

	@PatchMapping("/{id}/add-labels")
	public ResponseEntity<TaskResponseDto> addLabels(
		@RequestBody @Valid LabelIdsDto aDto,
		@PathVariable final int id
	) {
		var updatedTask = taskService.addLabels(aDto, id);

		return ResponseEntity.ok(new TaskResponseDto(updatedTask));
	}

	@PatchMapping("/{id}/remove-labels")
	public ResponseEntity<TaskResponseDto> removeLabels(
		@RequestBody @Valid LabelIdsDto aDto,
		@PathVariable final int id
	) {
		var updatedTask = taskService.removeLabels(aDto, id);

		return ResponseEntity.ok(new TaskResponseDto(updatedTask));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTaskById(@PathVariable final int id) {
		taskService.delete(id);

		return ResponseEntity.noContent().build();
	}
}

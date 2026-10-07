package com.example.task_manager.controllers;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.dtos.input.UpdateTaskDto;
import com.example.task_manager.dtos.output.ProjectResponseDto;
import com.example.task_manager.dtos.output.TaskResponseDto;
import com.example.task_manager.helpers.ConvertRepresentationModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.services.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
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
	public ResponseEntity<List<TaskResponseDto>> getAllTasks(
		@RequestParam(required = false) String search
	) {
		final var tasks = ConvertRepresentationModel
			.toList(taskService.findAll(search), TaskResponseDto::new);

		if  (!tasks.isEmpty()) {
			for (TaskResponseDto task : tasks) {
				int id  = task.getId();
				task.add(linkTo(methodOn(TaskController.class).getTaskById(id)).withSelfRel());
			}
		}

		return ResponseEntity.ok(tasks);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable final int id) {

		final var task = taskService.findById(id);

		return ResponseEntity.ok(new TaskResponseDto(task));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<TaskResponseDto> updateTaskById(
		@PathVariable final int id,
		@Valid @NonNull final UpdateTaskDto aDto
	) {
		final var updatedTasks = taskService.updateAndFlush(id, aDto);

		return ResponseEntity.ok(new TaskResponseDto(updatedTasks));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTaskById(@PathVariable final int id) {
		taskService.delete(id);

		return  ResponseEntity.noContent().build();
	}
}

package com.example.task_manager;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.LabelRepository;
import com.example.task_manager.repositories.ProjectRepository;
import com.example.task_manager.repositories.TaskRepository;
import com.example.task_manager.repositories.UserRepository;
import com.example.task_manager.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TaskManagerApplicationTests {

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskService taskService;

	@Test
	void contextLoads() {
	}

	@Test
	void findsLabelIdsUsingDatabasePagination() {
		var labels = labelRepository.saveAllAndFlush(java.util.List.of(
			new LabelModel(new CreateLabelDto("Alpha", "#111111")),
			new LabelModel(new CreateLabelDto("Beta", "#222222")),
			new LabelModel(new CreateLabelDto("Gamma", "#333333"))
		));

		var page = labelRepository.findAllIds(PageRequest.of(1, 2));

		assertEquals(3, page.getTotalElements());
		assertEquals(1, page.getContent().size());
		assertEquals(labels.get(2).getId(), page.getContent().get(0));
	}

	@Test
	@Transactional
	void findsProjectIdsUsingDatabasePagination() {
		var owner = new UserModel();
		owner.setName("Project Owner");
		owner.setEmail("project-owner@example.com");
		owner = userRepository.saveAndFlush(owner);

		var alpha = new ProjectModel(new CreateProjectDto("Alpha", "First", owner.getId()));
		var beta = new ProjectModel(new CreateProjectDto("Beta", "Second", owner.getId()));
		var gamma = new ProjectModel(new CreateProjectDto("Gamma", "Third", owner.getId()));
		alpha.setProject_owner(owner);
		beta.setProject_owner(owner);
		gamma.setProject_owner(owner);
		var projects = projectRepository.saveAllAndFlush(java.util.List.of(alpha, beta, gamma));

		var page = projectRepository.findAllIds(PageRequest.of(1, 2));

		assertEquals(3, page.getTotalElements());
		assertEquals(1, page.getContent().size());
		assertEquals(projects.get(2).getId(), page.getContent().get(0));
	}

	@Test
	void findsUserIdsUsingDatabasePagination() {
		var users = userRepository.saveAllAndFlush(java.util.List.of(
			createUser("Alpha", "alpha@example.com"),
			createUser("Beta", "beta@example.com"),
			createUser("Gamma", "gamma@example.com")
		));

		var page = userRepository.findAllIds(PageRequest.of(1, 2));

		assertEquals(3, page.getTotalElements());
		assertEquals(1, page.getContent().size());
		assertEquals(users.get(2).getId(), page.getContent().get(0));
	}

	@Test
	@Transactional
	void returnsTasksFromPaginatedIdQuery() {
		var owner = createUser("Task Owner", "task-owner@example.com");
		owner = userRepository.saveAndFlush(owner);

		var project = new ProjectModel(new CreateProjectDto(
			"Task pagination project",
			"Project for pagination test",
			owner.getId()
		));
		project.setProject_owner(owner);
		project = projectRepository.saveAndFlush(project);

		var firstTask = new TaskModel(
			new CreateTaskDto("Task pagination first", null, project.getId(), owner.getId(), null),
			project,
			owner,
			java.util.Set.of()
		);
		var secondTask = new TaskModel(
			new CreateTaskDto("Task pagination second", null, project.getId(), owner.getId(), null),
			project,
			owner,
			java.util.Set.of()
		);
		taskRepository.saveAllAndFlush(java.util.List.of(firstTask, secondTask));

		var page = taskService.findAll(null, PageRequest.of(0, 10));

		assertEquals(2, page.getTotalElements());
		assertEquals(2, page.getContent().size());
		assertEquals(
			java.util.List.of(firstTask.getId(), secondTask.getId()),
			page.getContent().stream().map(TaskModel::getId).toList()
		);
	}

	@Test
	@Transactional
	void filtersOverdueTasksAndCountsNonArchivedTasksByStatus() {
		var user = userRepository.saveAndFlush(createUser("Overdue Owner", "overdue-owner@example.com"));
		var project = new ProjectModel(new CreateProjectDto(
			"Overdue project",
			"Overdue task test project",
			user.getId()
		));
		project.setProject_owner(user);
		project = projectRepository.saveAndFlush(project);

		Map<TaskStatusEnum, Long> countsBefore = new EnumMap<>(TaskStatusEnum.class);
		for (var count : taskService.summarizeByStatus()) {
			countsBefore.put(count.status(), count.count());
		}

		var past = Instant.now().minusSeconds(86_400);
		var pending = createTask("Pending overdue", project, user, past);
		var running = createTask("Running overdue", project, user, past);
		running.start();
		var completed = createTask("Completed due", project, user, past);
		completed.start();
		completed.complete();
		var canceled = createTask("Canceled due", project, user, past);
		canceled.start();
		canceled.cancel();
		var archived = createTask("Archived due", project, user, past);
		archived.archive();
		taskRepository.saveAllAndFlush(java.util.List.of(pending, running, completed, canceled, archived));

		var overdueIds = taskRepository.findOverdueIds(PageRequest.of(0, 100))
			.getContent()
			.stream()
			.map(Number::intValue)
			.toList();
		assertTrue(overdueIds.contains(pending.getId()));
		assertTrue(overdueIds.contains(running.getId()));
		assertFalse(overdueIds.contains(completed.getId()));
		assertFalse(overdueIds.contains(canceled.getId()));
		assertFalse(overdueIds.contains(archived.getId()));
		assertFalse(archived.isOverdue());

		Map<TaskStatusEnum, Long> countsAfter = new EnumMap<>(TaskStatusEnum.class);
		for (var count : taskService.summarizeByStatus()) {
			countsAfter.put(count.status(), count.count());
		}
		for (TaskStatusEnum status : TaskStatusEnum.values()) {
			assertEquals(1L, countsAfter.get(status) - countsBefore.get(status));
		}
	}

	private TaskModel createTask(String title, ProjectModel project, UserModel user, Instant dueDate) {
		var task = new TaskModel(
			new CreateTaskDto(title, null, project.getId(), user.getId(), null),
			project,
			user,
			java.util.Set.of()
		);
		task.setDue_date(dueDate);
		return task;
	}

	private UserModel createUser(String name, String email) {
		var user = new UserModel();
		user.setName(name);
		user.setEmail(email);
		return user;
	}

}

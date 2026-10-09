package com.example.task_manager;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.CreateTaskDto;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:task_manager;DB_CLOSE_DELAY=-1",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.liquibase.enabled=false"
})
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

	private UserModel createUser(String name, String email) {
		var user = new UserModel();
		user.setName(name);
		user.setEmail(email);
		return user;
	}

}

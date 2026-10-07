package com.example.task_manager.repositories;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.context.annotation.Import;
import com.example.task_manager.services.ProjectService;
import com.example.task_manager.services.LabelService;
import com.example.task_manager.services.TaskService;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.liquibase.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import({ProjectService.class, LabelService.class, TaskService.class})
class FullTextSearchRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private UserModel user;
    private ProjectModel project;
    private TaskModel task;

    @BeforeEach
    void createSearchFixtures() {
        user = new UserModel();
        user.setName("Ada Lovelace");
        user.setEmail("ada@example.com");
        user = userRepository.saveAndFlush(user);

        project = new ProjectModel();
        project.setName("Blue roadmap");
        project.setDescription("Planning for release");
        project.setProject_owner(user);
        project = projectRepository.saveAndFlush(project);

        task = new TaskModel();
        task.setTitle("Write tests");
        task.setDescription("Cover PostgreSQL search");
        task.setProject(project);
        task.setUser(user);
        task.setPriority(TaskPriorityEnum.HIGH);
        task = taskRepository.saveAndFlush(task);

        labelRepository.saveAndFlush(
            new LabelModel(new CreateLabelDto("Yellow urgent", "#FFFF00"))
        );
    }

    @Test
    void searchesLabelsByEveryTerm() {
        List<LabelModel> results = labelService.findByName("yellow urgent");

        assertThat(results).extracting(LabelModel::getName).containsExactly("Yellow urgent");
    }

    @Test
    void loadsLabelTasksAfterRepositorySessionCloses() {
        List<LabelModel> results = labelService.findByName("yellow urgent");
        entityManager.clear();

        var persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(results.get(0), "tasks")).isTrue();
        assertThat(results.get(0).getTasks()).isEmpty();
    }

    @Test
    void searchesProjectsAcrossNameAndDescription() {
        List<ProjectModel> results = projectService.searchByNameAndDescription("blue release");

        assertThat(results).extracting(ProjectModel::getId)
            .containsExactly(project.getId());
    }

    @Test
    void loadsProjectRelationsForResponseMappingAfterRepositorySessionCloses() {
        List<ProjectModel> results = projectService.searchByNameAndDescription("blue release");
        entityManager.clear();

        assertThat(results.get(0).getProject_owner().getName()).isEqualTo("Ada Lovelace");
        assertThat(results.get(0).getMembers()).isEmpty();
    }

    @Test
    void searchesTasksAcrossTitleAndDescription() {
        List<TaskModel> results = taskService.searchByTitleAndDescription("tests PostgreSQL");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Write tests");
    }

    @Test
    void loadsTaskRelationsAfterRepositorySessionCloses() {
        List<TaskModel> results = taskService.searchByTitleAndDescription("tests PostgreSQL");
        entityManager.clear();

        TaskModel result = results.get(0);
        var persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(result, "project")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(result, "user")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(result, "labels")).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(result.getProject(), "project_owner")).isTrue();
        assertThat(result.getProject().getProject_owner().getName()).isEqualTo("Ada Lovelace");
        assertThat(result.getUser().getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void searchesUsersAcrossNameAndEmail() {
        List<UserModel> resultsByName = userRepository.searchByNameAndEmail("Lovelace");
        List<UserModel> resultsByEmail = userRepository.searchByNameAndEmail("ada@example.com");

        assertThat(resultsByName).extracting(UserModel::getId).containsExactly(user.getId());
        assertThat(resultsByEmail).extracting(UserModel::getId).containsExactly(user.getId());
    }
}

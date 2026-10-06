package com.example.task_manager.repositories;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
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
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private UserModel user;
    private ProjectModel project;

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

        var task = new TaskModel();
        task.setTitle("Write tests");
        task.setDescription("Cover PostgreSQL search");
        task.setProject(project);
        task.setUser(user);
        task.setPriority(TaskPriorityEnum.HIGH);
        task = taskRepository.saveAndFlush(task);

        labelRepository.saveAndFlush(new LabelModel(new CreateLabelDto("Yellow urgent", "#FFFF00")));
    }

    @Test
    void searchesLabelsByEveryTerm() {
        List<LabelModel> results = labelRepository.findByName("yellow urgent");

        assertThat(results).extracting(LabelModel::getName).containsExactly("Yellow urgent");
    }

    @Test
    void searchesProjectsAcrossNameAndDescription() {
        List<ProjectModel> results = projectRepository.searchByNameAndDescription("blue release");

        assertThat(results).extracting(ProjectModel::getId)
            .containsExactly(project.getId());
    }

    @Test
    void searchesTasksAcrossTitleAndDescription() {
        List<TaskModel> results = taskRepository.searchByTitleAndDescription("tests PostgreSQL");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Write tests");
    }

    @Test
    void searchesUsersAcrossNameAndEmail() {
        List<UserModel> resultsByName = userRepository.searchByNameAndEmail("Lovelace");
        List<UserModel> resultsByEmail = userRepository.searchByNameAndEmail("ada@example.com");

        assertThat(resultsByName).extracting(UserModel::getId).containsExactly(user.getId());
        assertThat(resultsByEmail).extracting(UserModel::getId).containsExactly(user.getId());
    }
}

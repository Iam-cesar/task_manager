package com.example.task_manager.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.UpdateProjectDto;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.services.ProjectService;

@WebMvcTest(ProjectsController.class)
class ProjectsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private UserModel mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new UserModel();
        ReflectionTestUtils.setField(mockOwner, "id", 1);
        mockOwner.setName("Owner User");
        mockOwner.setEmail("owner@example.com");
    }

    private ProjectModel createMockProject(Integer id, String name, String description, UserModel owner) {
        ProjectModel project = new ProjectModel();
        ReflectionTestUtils.setField(project, "id", id);
        project.setName(name);
        project.setDescription(description);
        project.setProject_owner(owner);
        return project;
    }

    @Nested
    @DisplayName("POST /projects")
    class CreateProjectTests {

        @Test
        @DisplayName("Should return 201 Created with Location header and project body")
        void shouldCreateProjectSuccessfully() throws Exception {
            ProjectModel savedProject = createMockProject(1, "Task Manager API", "Spring Boot Project", mockOwner);
            when(projectService.saveAndFlush(any(CreateProjectDto.class))).thenReturn(savedProject);

            String requestBody = """
                {
                    "name": "Task Manager API",
                    "description": "Spring Boot Project",
                    "project_owner_id": 1
                }
                """;

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/projects/1")))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Task Manager API")))
                    .andExpect(jsonPath("$.description", is("Spring Boot Project")))
                    .andExpect(jsonPath("$.status", is("active")))
                    .andExpect(jsonPath("$.project_owner.id", is(1)))
                    .andExpect(jsonPath("$.project_owner.name", is("Owner User")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when project owner id is missing in service")
        void shouldReturn400WhenProjectOwnerIdIsMissing() throws Exception {
            when(projectService.saveAndFlush(any(CreateProjectDto.class)))
                    .thenThrow(new IllegalArgumentException("Project owner id must not be null"));

            String requestBody = """
                {
                    "name": "Task Manager API",
                    "description": "Spring Boot Project"
                }
                """;

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", is("Project owner id must not be null")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when project owner does not exist")
        void shouldReturn404WhenOwnerNotFound() throws Exception {
            when(projectService.saveAndFlush(any(CreateProjectDto.class)))
                    .thenThrow(new UserNotFoundException("User not found"));

            String requestBody = """
                {
                    "name": "Task Manager API",
                    "description": "Spring Boot Project",
                    "project_owner_id": 999
                }
                """;

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("User not found")));
        }

        @Test
        @DisplayName("Should return 409 Conflict when project with same name already exists for user")
        void shouldReturn409WhenProjectAlreadyExists() throws Exception {
            when(projectService.saveAndFlush(any(CreateProjectDto.class)))
                    .thenThrow(new ProjectAlreadyExistsException("Já existe um projeto com esse nome para este usuário"));

            String requestBody = """
                {
                    "name": "Task Manager API",
                    "description": "Spring Boot Project",
                    "project_owner_id": 1
                }
                """;

            mockMvc.perform(post("/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is("Já existe um projeto com esse nome para este usuário")));
        }
    }

    @Nested
    @DisplayName("GET /projects")
    class GetAllProjectsTests {

        @Test
        @DisplayName("Should return 200 OK with list of projects and HATEOAS self links")
        void shouldReturnAllProjectsWithHateoasLinks() throws Exception {
            ProjectModel project1 = createMockProject(1, "Project Alpha", "First project", mockOwner);
            ProjectModel project2 = createMockProject(2, "Project Beta", "Second project", mockOwner);
            when(projectService.findAll(null)).thenReturn(List.of(project1, project2));

            mockMvc.perform(get("/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("Project Alpha")))
                    .andExpect(jsonPath("$[0].links[0].rel", is("self")))
                    .andExpect(jsonPath("$[0].links[0].href", containsString("/projects/1")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Project Beta")))
                    .andExpect(jsonPath("$[1].links[0].rel", is("self")))
                    .andExpect(jsonPath("$[1].links[0].href", containsString("/projects/2")));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no projects exist")
        void shouldReturnEmptyListWhenNoProjects() throws Exception {
            when(projectService.findAll(null)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/projects"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should search projects when search query parameter is provided")
        void shouldSearchProjectsByQueryParameter() throws Exception {
            ProjectModel project = createMockProject(1, "Blue roadmap", "Planning for release", mockOwner);
            when(projectService.findAll("blue release"))
                .thenReturn(List.of(project));

            mockMvc.perform(get("/projects").param("search", "blue release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Blue roadmap")))
                .andExpect(jsonPath("$[0].links[0].href", containsString("/projects/1")));

            verify(projectService).findAll("blue release");
        }
    }

    @Nested
    @DisplayName("GET /projects/{id}")
    class GetProjectByIdTests {

        @Test
        @DisplayName("Should return 200 OK with project and HATEOAS link to all projects")
        void shouldReturnProjectByIdWithHateoasLink() throws Exception {
            ProjectModel project = createMockProject(1, "Task Manager API", "Spring Boot Project", mockOwner);
            when(projectService.findById(1)).thenReturn(project);

            mockMvc.perform(get("/projects/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Task Manager API")))
                    .andExpect(jsonPath("$.description", is("Spring Boot Project")))
                    .andExpect(jsonPath("$.project_owner.name", is("Owner User")))
                    .andExpect(jsonPath("$._links.projects.href", containsString("/projects")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when project does not exist")
        void shouldReturn404WhenProjectNotFound() throws Exception {
            when(projectService.findById(999)).thenThrow(new ProjectNotFoundException("Project not found"));

            mockMvc.perform(get("/projects/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Project not found")));
        }
    }

    @Nested
    @DisplayName("PATCH /projects/{id}")
    class UpdateProjectTests {

        @Test
        @DisplayName("Should return 200 OK with updated project")
        void shouldUpdateProjectSuccessfully() throws Exception {
            ProjectModel updatedProject = createMockProject(1, "Updated Project Name", "Updated Description", mockOwner);
            when(projectService.updateAndFlush(eq(1), any(UpdateProjectDto.class))).thenReturn(updatedProject);

            String requestBody = """
                {
                    "name": "Updated Project Name",
                    "description": "Updated Description"
                }
                """;

            mockMvc.perform(patch("/projects/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated Project Name")))
                    .andExpect(jsonPath("$.description", is("Updated Description")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existing project")
        void shouldReturn404WhenUpdatingNonExistingProject() throws Exception {
            when(projectService.updateAndFlush(eq(999), any(UpdateProjectDto.class)))
                    .thenThrow(new ProjectNotFoundException("Project not found"));

            String requestBody = """
                {
                    "name": "Updated Project Name"
                }
                """;

            mockMvc.perform(patch("/projects/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Project not found")));
        }

        @Test
        @DisplayName("Should return 409 Conflict when updating to a name already in use by the owner")
        void shouldReturn409WhenUpdatingToExistingProjectName() throws Exception {
            when(projectService.updateAndFlush(eq(1), any(UpdateProjectDto.class)))
                    .thenThrow(new ProjectAlreadyExistsException("Já existe um projeto com esse nome para este usuário"));

            String requestBody = """
                {
                    "name": "Duplicate Project Name"
                }
                """;

            mockMvc.perform(patch("/projects/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is("Já existe um projeto com esse nome para este usuário")));
        }
    }

    @Nested
    @DisplayName("DELETE /projects/{id}")
    class DeleteProjectTests {

        @Test
        @DisplayName("Should return 204 No Content when deleting existing project")
        void shouldDeleteProjectSuccessfully() throws Exception {
            doNothing().when(projectService).delete(1);

            mockMvc.perform(delete("/projects/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existing project")
        void shouldReturn404WhenDeletingNonExistingProject() throws Exception {
            doThrow(new ProjectNotFoundException("Project not found")).when(projectService).delete(999);

            mockMvc.perform(delete("/projects/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("Project not found")));
        }
    }
}

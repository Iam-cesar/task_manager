package com.example.task_manager.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.dtos.input.UpdateProjectDto;
import com.example.task_manager.exceptions.ProjectAlreadyExistsException;
import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.ProjectMemberRepository;
import com.example.task_manager.repositories.ProjectRepository;
import com.example.task_manager.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private UserModel mockOwner;

    @BeforeEach
    void setUp() {
        mockOwner = new UserModel();
        ReflectionTestUtils.setField(mockOwner, "id", 1);
        mockOwner.setName("Owner User");
        mockOwner.setEmail("owner@example.com");
    }

    @Nested
    @DisplayName("saveAndFlush")
    class SaveAndFlushTests {

        @Test
        @DisplayName("Should save project successfully when project name is unique for owner")
        void shouldSaveProjectSuccessfully() {
            CreateProjectDto dto = new CreateProjectDto("New Project", "Description", 1);
            when(userRepository.findById(1)).thenReturn(Optional.of(mockOwner));
            when(projectRepository.existsByProjectOwnerIdAndName(1, "New Project")).thenReturn(false);
            when(projectRepository.saveAndFlush(any(ProjectModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ProjectModel result = projectService.saveAndFlush(dto);

            assertNotNull(result);
            assertEquals("New Project", result.getName());
            assertEquals(mockOwner, result.getProject_owner());
            verify(projectRepository).saveAndFlush(any(ProjectModel.class));
        }

        @Test
        @DisplayName("Should throw ProjectAlreadyExistsException when project with same name already exists for owner")
        void shouldThrowExceptionWhenProjectNameAlreadyExistsForOwner() {
            CreateProjectDto dto = new CreateProjectDto("Existing Project", "Description", 1);
            when(userRepository.findById(1)).thenReturn(Optional.of(mockOwner));
            when(projectRepository.existsByProjectOwnerIdAndName(1, "Existing Project")).thenReturn(true);

            assertThrows(ProjectAlreadyExistsException.class, () -> projectService.saveAndFlush(dto));
            verify(projectRepository, never()).saveAndFlush(any(ProjectModel.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when project owner id is null")
        void shouldThrowExceptionWhenOwnerIdIsNull() {
            CreateProjectDto dto = new CreateProjectDto("New Project", "Description", null);

            assertThrows(IllegalArgumentException.class, () -> projectService.saveAndFlush(dto));
        }
    }

    @Nested
    @DisplayName("updateAndFlush")
    class UpdateAndFlushTests {

        @Test
        @DisplayName("Should update project successfully when name is changed to an unused name")
        void shouldUpdateProjectSuccessfully() {
            ProjectModel existing = new ProjectModel();
            ReflectionTestUtils.setField(existing, "id", 1);
            existing.setName("Old Name");
            existing.setProject_owner(mockOwner);

            when(projectRepository.findByIdWithRelations(1)).thenReturn(Optional.of(existing));
            when(projectRepository.existsByProjectOwnerIdAndName(1, "New Name")).thenReturn(false);
            when(projectRepository.saveAndFlush(any(ProjectModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProjectDto dto = new UpdateProjectDto("New Name", "New Description");
            ProjectModel result = projectService.updateAndFlush(1, dto);

            assertEquals("New Name", result.getName());
            assertEquals("New Description", result.getDescription());
        }

        @Test
        @DisplayName("Should throw ProjectAlreadyExistsException when updated name already exists for owner")
        void shouldThrowExceptionWhenUpdatedNameAlreadyExists() {
            ProjectModel existing = new ProjectModel();
            ReflectionTestUtils.setField(existing, "id", 1);
            existing.setName("Old Name");
            existing.setProject_owner(mockOwner);

            when(projectRepository.findByIdWithRelations(1)).thenReturn(Optional.of(existing));
            when(projectRepository.existsByProjectOwnerIdAndName(1, "Existing Name")).thenReturn(true);

            UpdateProjectDto dto = new UpdateProjectDto("Existing Name", "New Description");
            assertThrows(ProjectAlreadyExistsException.class, () -> projectService.updateAndFlush(1, dto));
            verify(projectRepository, never()).saveAndFlush(any(ProjectModel.class));
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should delete project when project exists")
        void shouldDeleteProjectWhenProjectExists() {
            ProjectModel project = new ProjectModel();
            ReflectionTestUtils.setField(project, "id", 1);
            project.setName("Sample Project");

            when(projectRepository.findByIdWithRelations(1)).thenReturn(Optional.of(project));

            assertDoesNotThrow(() -> projectService.delete(1));

            verify(projectRepository).findByIdWithRelations(1);
            verify(projectRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project does not exist")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectRepository.findByIdWithRelations(999)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> projectService.delete(999));

            verify(projectRepository).findByIdWithRelations(999);
        }
    }
}

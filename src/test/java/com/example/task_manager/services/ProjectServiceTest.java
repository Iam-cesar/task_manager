package com.example.task_manager.services;

import com.example.task_manager.exceptions.ProjectNotFoundException;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.repositories.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should delete project when project exists")
        void shouldDeleteProjectWhenProjectExists() {
            ProjectModel project = new ProjectModel();
            ReflectionTestUtils.setField(project, "id", 1);
            project.setName("Sample Project");

            when(projectRepository.findById(1)).thenReturn(Optional.of(project));

            assertDoesNotThrow(() -> projectService.delete(1));

            verify(projectRepository).findById(1);
            verify(projectRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project does not exist")
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> projectService.delete(999));

            verify(projectRepository).findById(999);
        }
    }
}

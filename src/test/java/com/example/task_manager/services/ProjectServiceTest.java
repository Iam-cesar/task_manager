package com.example.task_manager.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Should return a page with relations in the same order as the paged IDs")
        void shouldReturnPageOfProjectsInIdPageOrder() {
            var pageable = PageRequest.of(1, 2);
            var first = new ProjectModel(new CreateProjectDto("Alpha", "Description", 1));
            var second = new ProjectModel(new CreateProjectDto("Beta", "Description", 1));
            ReflectionTestUtils.setField(first, "id", 10);
            ReflectionTestUtils.setField(second, "id", 20);
            Page<Number> idsPage = new PageImpl<>(List.of(10, 20), pageable, 5);
            when(projectRepository.findAllIds(pageable)).thenReturn(idsPage);
            when(projectRepository.findAllWithRelationsByIdIn(List.of(10, 20)))
                .thenReturn(List.of(second, first));

            var result = projectService.findAll(null, pageable);

            assertEquals(List.of(first, second), result.getContent());
            assertEquals(5, result.getTotalElements());
            assertEquals(3, result.getTotalPages());
            verify(projectRepository).findAllIds(pageable);
            verify(projectRepository).findAllWithRelationsByIdIn(List.of(10, 20));
        }

        @Test
        @DisplayName("Should use the paged full-text search when a search term is supplied")
        void shouldReturnPageOfSearchResults() {
            var pageable = PageRequest.of(0, 2);
            var project = new ProjectModel(new CreateProjectDto("Blue roadmap", "Release plan", 1));
            ReflectionTestUtils.setField(project, "id", 7);
            when(projectRepository.searchIdsByNameAndDescription("blue release", pageable))
                .thenReturn(new PageImpl<>(List.of(7L), pageable, 1));
            when(projectRepository.findAllWithRelationsByIdIn(List.of(7)))
                .thenReturn(List.of(project));

            var result = projectService.findAll("blue release", pageable);

            assertEquals(List.of(project), result.getContent());
            assertEquals(1, result.getTotalElements());
            verify(projectRepository).searchIdsByNameAndDescription("blue release", pageable);
        }

        @Test
        @DisplayName("Should not fetch project relations when the requested page has no IDs")
        void shouldReturnEmptyPageWithoutFetchingRelations() {
            var pageable = PageRequest.of(3, 2);
            when(projectRepository.findAllIds(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 5));

            var result = projectService.findAll(" ", pageable);

            assertEquals(List.of(), result.getContent());
            assertEquals(5, result.getTotalElements());
            verify(projectRepository, never()).findAllWithRelationsByIdIn(any());
        }
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

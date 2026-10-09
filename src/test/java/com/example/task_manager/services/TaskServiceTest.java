package com.example.task_manager.services;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.exceptions.LabelNotFoundException;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.LabelRepository;
import com.example.task_manager.repositories.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createsTaskWithOnlyRequestedLabels() {
        var user = new UserModel();
        var project = new ProjectModel();
        var requestedLabel = new LabelModel(new CreateLabelDto("Requested", "#112233"));
        var savedTask = new AtomicReference<TaskModel>();

        when(userService.findById(1)).thenReturn(user);
        when(projectService.findById(1)).thenReturn(project);
        when(labelRepository.findAllById(List.of(4))).thenReturn(List.of(requestedLabel));
        when(taskRepository.saveAndFlush(any(TaskModel.class)))
            .thenAnswer(invocation -> {
                var task = (TaskModel) invocation.getArgument(0);
                ReflectionTestUtils.setField(task, "id", 15);
                savedTask.set(task);
                return task;
            });
        when(taskRepository.findWithRelationsById(15))
            .thenAnswer(invocation -> Optional.of(savedTask.get()));

        var createdTask = taskService.saveAndFlush(
            new CreateTaskDto("Wash dishes", null, 1, 1, List.of(4))
        );

        assertEquals(List.of(requestedLabel), List.copyOf(createdTask.getLabels()));
        verify(labelRepository).findAllById(List.of(4));
        verify(taskRepository).findWithRelationsById(15);
    }

    @Test
    void rejectsMissingRequestedLabels() {
        when(userService.findById(1)).thenReturn(new UserModel());
        when(projectService.findById(1)).thenReturn(new ProjectModel());
        when(labelRepository.findAllById(List.of(4))).thenReturn(List.of());

        assertThrows(
            LabelNotFoundException.class,
            () -> taskService.saveAndFlush(
                new CreateTaskDto("Wash dishes", null, 1, 1, List.of(4))
            )
        );
        verify(taskRepository, never()).saveAndFlush(any(TaskModel.class));
    }

    @Test
    void findsTaskByIdWithRelations() {
        var task = new TaskModel();
        when(taskRepository.findWithRelationsById(15)).thenReturn(Optional.of(task));

        assertEquals(task, taskService.findById(15));
    }

    @Test
    void returnsAllTasksWithRelationsWhenSearchIsBlank() {
        var pageable = PageRequest.of(0, 10);
        var task = new TaskModel();
        ReflectionTestUtils.setField(task, "id", 15);
        when(taskRepository.findAllIds(pageable))
            .thenReturn(new PageImpl<>(List.of(15), pageable, 1));
        when(taskRepository.findAllWithRelationsByIdIn(List.of(15)))
            .thenReturn(List.of(task));

        assertEquals(List.of(task), taskService.findAll(" ", pageable).getContent());
        verify(taskRepository).findAllIds(pageable);
        verify(taskRepository, never()).searchIdsByTitleAndDescription(any(), any());
    }

    @Test
    void returnsSearchResultsWithRelations() {
        var pageable = PageRequest.of(0, 10);
        var task = new TaskModel();
        ReflectionTestUtils.setField(task, "id", 15);
        when(taskRepository.searchIdsByTitleAndDescription("dishes", pageable))
            .thenReturn(new PageImpl<>(List.of(15), pageable, 1));
        when(taskRepository.findAllWithRelationsByIdIn(List.of(15))).thenReturn(List.of(task));

        assertEquals(List.of(task), taskService.findAll("dishes", pageable).getContent());
        verify(taskRepository).findAllWithRelationsByIdIn(List.of(15));
    }
}

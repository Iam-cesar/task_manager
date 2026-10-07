package com.example.task_manager.dtos.output;

import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskResponseDtoTest {

    @Test
    void doesNotLoadTasksWhenMappingLabelsOnTaskResponse() {
        var task = mock(TaskModel.class);
        var user = mock(UserModel.class);
        var project = mock(ProjectModel.class);
        var label = mock(LabelModel.class);

        when(task.getUser()).thenReturn(user);
        when(task.getProject()).thenReturn(project);
        when(task.getLabels()).thenReturn(Set.of(label));
        when(project.getProject_owner()).thenReturn(user);
        when(project.getMembers()).thenReturn(Set.of());

        var response = new TaskResponseDto(task);

        assertTrue(response.getLabels().get(0).getTasks().isEmpty());
        verify(label, never()).getTasks();
    }
}

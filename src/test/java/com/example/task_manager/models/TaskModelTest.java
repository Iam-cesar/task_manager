package com.example.task_manager.models;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.exceptions.InvalidTaskTransitionException;
import com.example.task_manager.exceptions.TaskNotEditableException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskModelTest {

    @Test
    void copiesDueDateFromCreationContract() {
        var dueDate = Instant.now().plusSeconds(3_600);
        var task = new TaskModel(
            new CreateTaskDto("Task title", "Description", 1, 1, null, dueDate),
            new ProjectModel(),
            new UserModel(),
            Set.of()
        );

        assertEquals("Task title", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(dueDate, task.getDue_date());
    }

    @Test
    void allowsOnlyPendingToRunningThenCompletionOrCancellation() {
        var task = new TaskModel();
        assertThrows(InvalidTaskTransitionException.class, () -> task.changeStatus(TaskStatusEnum.COMPLETED));

        task.changeStatus(TaskStatusEnum.RUNNING);
        task.changeStatus(TaskStatusEnum.COMPLETED);

        assertNotNull(task.getCompletion_date());
        assertThrows(TaskNotEditableException.class, () -> task.changeStatus(TaskStatusEnum.PENDING));
        assertThrows(TaskNotEditableException.class, () -> task.setTitle("Changed title"));
    }

    @Test
    void rejectsContentAndLabelChangesAfterArchiving() {
        var task = new TaskModel();
        task.archive();

        assertTrue(task.isArchived());
        assertThrows(TaskNotEditableException.class, () -> task.setDescription("Changed"));
        assertThrows(TaskNotEditableException.class, () -> task.addLabel(new LabelModel()));
        assertThrows(TaskNotEditableException.class, () -> task.changeStatus(TaskStatusEnum.RUNNING));
    }

    @Test
    void rejectsContentChangesAfterCancellation() {
        var task = new TaskModel();
        task.changeStatus(TaskStatusEnum.RUNNING);
        task.changeStatus(TaskStatusEnum.CANCELED);

        assertThrows(TaskNotEditableException.class, () -> task.setPriority(null));
        task.archive();
        assertTrue(task.isArchived());
        assertThrows(TaskNotEditableException.class, () -> task.setTitle("Changed title"));
    }
}

package com.example.task_manager.exceptions;

public class TaskDueDateInPastException extends RuntimeException {
    public TaskDueDateInPastException() {
        super("Due date must be in the future");
    }
}

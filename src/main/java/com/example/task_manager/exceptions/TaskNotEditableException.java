package com.example.task_manager.exceptions;

public class TaskNotEditableException extends RuntimeException {
    public TaskNotEditableException(String message) {
        super(message);
    }
}

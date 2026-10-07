package com.example.task_manager.exceptions;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException () { super("Task not found"); }

    public TaskNotFoundException (final String message) { super(message); }

    public TaskNotFoundException (final String message, final Throwable cause) { super(message, cause); }
}

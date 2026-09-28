package com.example.task_manager.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException() { super("Project not found"); }

    public ProjectNotFoundException(String message) { super(message); }

    public ProjectNotFoundException(String message, Throwable cause) { super(message, cause); }
}

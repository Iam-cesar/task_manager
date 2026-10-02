package com.example.task_manager.exceptions;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException() { super("Project not found"); }

    public ProjectNotFoundException(final String message) { super(message); }

    public ProjectNotFoundException(final String message, final Throwable cause) { super(message, cause); }
}

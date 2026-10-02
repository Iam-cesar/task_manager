package com.example.task_manager.exceptions;

public class ProjectAlreadyExistsException extends RuntimeException {

    public ProjectAlreadyExistsException() {
        super("A project with this name already exists for this user.");
    }

    public ProjectAlreadyExistsException(final String message) { super(message); }

    public ProjectAlreadyExistsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ProjectAlreadyExistsException(final Throwable cause) {
        super(cause);
    }
}

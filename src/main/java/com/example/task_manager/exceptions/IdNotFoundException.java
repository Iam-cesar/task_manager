package com.example.task_manager.exceptions;

public class IdNotFoundException extends RuntimeException {

    public IdNotFoundException(final String message) {
        super(message);
    }

    public IdNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IdNotFoundException(final Throwable cause) {
        super(cause);
    }
}

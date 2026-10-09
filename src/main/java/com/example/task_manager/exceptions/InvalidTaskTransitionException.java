package com.example.task_manager.exceptions;

public class InvalidTaskTransitionException extends RuntimeException {
    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}

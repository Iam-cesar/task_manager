package com.example.task_manager.exceptions;

public class UserHasTasksException extends RuntimeException {
    public UserHasTasksException() {
        super("Cannot delete a user who has assigned tasks");
    }
}

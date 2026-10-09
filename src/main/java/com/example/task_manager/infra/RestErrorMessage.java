package com.example.task_manager.infra;

import org.springframework.http.HttpStatus;

import java.util.List;

public record RestErrorMessage(HttpStatus status, String message, List<FieldError> errors) {
    public RestErrorMessage {
        errors = List.copyOf(errors);
    }

    public RestErrorMessage(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    public record FieldError(String field, String message) {
    }
}

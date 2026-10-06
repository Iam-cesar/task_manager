package com.example.task_manager.infra;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Setter;

public record RestErrorMessage(HttpStatus status, String message) {
}

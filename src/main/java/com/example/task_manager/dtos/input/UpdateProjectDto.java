package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.Size;

public record UpdateProjectDto(
        @Size(min = 1, max = 60) String name,

        @Size(min = 1, max = 255) String description
) {
}

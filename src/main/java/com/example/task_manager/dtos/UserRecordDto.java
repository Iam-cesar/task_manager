package com.example.task_manager.dtos;

import com.example.task_manager.enums.UserStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRecordDto(
        @NotBlank
        @Size(max = 60)
        String name,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotNull
        UserStatusEnum userStatus
) {}

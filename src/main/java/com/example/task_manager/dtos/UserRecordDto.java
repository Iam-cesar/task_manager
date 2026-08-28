package com.example.task_manager.dtos;

import com.example.task_manager.enums.UserStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public record UserRecordDto(
        @NotBlank
        @Max(60)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        UserStatusEnum userStatus
) {};

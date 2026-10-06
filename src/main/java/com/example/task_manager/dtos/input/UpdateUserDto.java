package com.example.task_manager.dtos.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(

    @Size(min = 1, max = 60) String name,

    @Email @Size(max = 254) String email
) {

}

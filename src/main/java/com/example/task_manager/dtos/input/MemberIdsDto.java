package com.example.task_manager.dtos.input;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record MemberIdsDto(
        @NotEmpty(message = "The members list should not be empty.")
        List<Integer> member_ids
) {
}

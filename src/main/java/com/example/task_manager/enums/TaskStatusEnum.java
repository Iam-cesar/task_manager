package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TaskStatusEnum {
    PENDING("pending"),
    RUNNING("running"),
    CANCELED("canceled"),
    COMPLETED("completed");

    @JsonValue
    private final String value;

    TaskStatusEnum(String value) {
        this.value = value;
    }
}

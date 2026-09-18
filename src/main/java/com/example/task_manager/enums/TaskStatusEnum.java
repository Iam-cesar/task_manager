package com.example.task_manager.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {
    PENDING("pending"),
    RUNNING("running"),
    CANCELED("canceled"),
    COMPLETED("completed");

    private final String value;

    TaskStatusEnum(String value) {
        this.value = value;
    }
}

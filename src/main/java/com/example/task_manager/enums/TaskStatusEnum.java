package com.example.task_manager.enums;

public enum TaskStatusEnum {
    PENDING("pending"),
    RUNNING("running"),
    CANCELED("canceled"),
    COMPLETED("completed");

    private final String value;

    TaskStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() { return this.value; }
}

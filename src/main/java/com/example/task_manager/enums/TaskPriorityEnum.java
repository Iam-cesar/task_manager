package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriorityEnum {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    @JsonValue
    private final String status;

    TaskPriorityEnum(final String anStatus) { this.status = anStatus; }

    public final String value() { return this.status; }
}

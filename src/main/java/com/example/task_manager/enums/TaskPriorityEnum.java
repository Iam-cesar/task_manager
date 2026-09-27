package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskPriorityEnum {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    @JsonValue
    private final String value;

    TaskPriorityEnum(String value) { this.value = value; }

    public String value() { return this.value; }
}

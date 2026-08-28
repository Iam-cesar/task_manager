package com.example.task_manager.enums;

public enum TaskPriorityEnum {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    URGENT("urgent");

    private final String value;

    TaskPriorityEnum(String value) { this.value = value; }

    public String value() { return this.value; }
}

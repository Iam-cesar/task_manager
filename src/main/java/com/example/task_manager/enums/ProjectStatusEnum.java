package com.example.task_manager.enums;

public enum ProjectStatusEnum {
    INACTIVE("inactive"), ACTIVE("active");

    private final String value;

    ProjectStatusEnum(String value) { this.value = value; }

    public String value() { return this.value; }
}

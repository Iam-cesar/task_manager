package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatusEnum {
    INACTIVE("inactive"), ACTIVE("active");

    @JsonValue
    private final String value;

    ProjectStatusEnum(String value) { this.value = value; }

    public String value() { return this.value; }
}

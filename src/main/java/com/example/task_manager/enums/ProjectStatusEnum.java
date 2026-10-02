package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatusEnum {
    INACTIVE("inactive"), ACTIVE("active");

    @JsonValue
    private final String status;

    ProjectStatusEnum(final String anStatus) { this.status = anStatus; }

    public final String value() { return this.status; }
}

package com.example.task_manager.enums;

public enum UserStatusEnum {
    ACTIVE("active"),
    INACTIVE("inactive");

    public final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() { return this.value; }
}
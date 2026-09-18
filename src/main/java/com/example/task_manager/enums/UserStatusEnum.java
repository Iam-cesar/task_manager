package com.example.task_manager.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ACTIVE("active"),
    INACTIVE("inactive");

    public final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }
}
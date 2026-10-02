package com.example.task_manager.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ACTIVE("active"),
    INACTIVE("inactive");

    @JsonValue
    public final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }
}
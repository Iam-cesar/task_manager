package com.example.task_manager.repositories;

import com.example.task_manager.enums.TaskStatusEnum;

public interface TaskStatusCountProjection {
    TaskStatusEnum getStatus();

    long getCount();
}

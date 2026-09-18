package com.example.task_manager.repositories;

import com.example.task_manager.models.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskModel, Integer> {
}

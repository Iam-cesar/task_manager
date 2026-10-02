package com.example.task_manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.TaskModel;

public interface TaskRepository extends JpaRepository<TaskModel, Integer> {
}

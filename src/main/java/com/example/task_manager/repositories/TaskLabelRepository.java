package com.example.task_manager.repositories;

import com.example.task_manager.models.TaskLabelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface TaskLabelRepository extends JpaRepository<TaskLabelModel, Integer> {
}

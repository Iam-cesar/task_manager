package com.example.task_manager.repositories;

import com.example.task_manager.models.ProjectModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectModel, Integer> {
}

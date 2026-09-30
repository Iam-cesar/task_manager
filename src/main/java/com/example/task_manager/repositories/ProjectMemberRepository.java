package com.example.task_manager.repositories;

import com.example.task_manager.models.ProjectMemberModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Integer> {
}

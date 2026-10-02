package com.example.task_manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.LabelModel;

public interface LabelRepository extends JpaRepository<LabelModel, Integer> {
}

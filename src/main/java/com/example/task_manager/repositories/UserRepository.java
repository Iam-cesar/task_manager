package com.example.task_manager.repositories;

import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
}

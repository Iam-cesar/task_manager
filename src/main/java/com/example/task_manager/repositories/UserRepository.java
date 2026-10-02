package com.example.task_manager.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByEmail(String email);
}

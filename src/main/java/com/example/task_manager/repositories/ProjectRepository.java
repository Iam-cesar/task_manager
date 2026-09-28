package com.example.task_manager.repositories;

import com.example.task_manager.dtos.ProjectResponseDto;
import com.example.task_manager.models.ProjectModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectModel, Integer> {

    @Query("""
        select new com.example.task_manager.dtos.ProjectResponseDto(
            p.id, p.name, p.description, count(t))
            from ProjectModel p
            left join TaskModel t on t.project = p
            where p.id = :id
            group by p.id, p.name, p.description
        """)
    Optional<ProjectResponseDto> findWithTaskCount(Integer id);
}

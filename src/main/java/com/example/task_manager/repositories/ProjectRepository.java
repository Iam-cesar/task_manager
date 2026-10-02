package com.example.task_manager.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_manager.dtos.output.ProjectResponseDto;
import com.example.task_manager.models.ProjectModel;

public interface ProjectRepository extends JpaRepository<ProjectModel, Integer> {

    @Query("""
        SELECT new com.example.task_manager.dtos.ProjectResponseDto(
            p.id, p.name, p.description, count(t))
            FROM ProjectModel p
            LEFT JOIN TaskModel t ON t.project = p
            WHERE p.id = :id
            GROUP BY p.id, p.name, p.description
        """)
    Optional<ProjectResponseDto> findWithTaskCount(@Param("id") Integer id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ProjectModel p WHERE p.project_owner.id = :ownerId AND p.name = :name")
    boolean existsByProjectOwnerIdAndName(@Param("ownerId") Integer ownerId, @Param("name") String name);

    @Query("""
            SELECT DISTINCT p FROM ProjectModel p
            LEFT JOIN FETCH p.project_owner
            LEFT JOIN FETCH p.members
        """)
    List<ProjectModel> findAllWithRelations();

    @Query("""
        SELECT DISTINCT p FROM ProjectModel p
        LEFT JOIN FETCH p.project_owner
        LEFT JOIN FETCH p.members
        WHERE p.id = :id
    """)
    Optional<ProjectModel> findByIdWithRelations(@Param("id") Integer id);
}

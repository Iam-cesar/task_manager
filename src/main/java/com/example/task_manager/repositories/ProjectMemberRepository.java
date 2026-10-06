package com.example.task_manager.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_manager.models.ProjectMemberModel;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberModel, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProjectMemberModel pm WHERE pm.project.id = :projectId AND pm.user.id IN :userIds")
    void deleteByProjectIDAndUserIdsIn (
        @Param("projectId") Integer projectId,
        @Param("userIds") List<Integer> userIds
    );
}

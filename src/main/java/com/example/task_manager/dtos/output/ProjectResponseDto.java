package com.example.task_manager.dtos.output;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import com.example.task_manager.enums.ProjectStatusEnum;
import com.example.task_manager.models.ProjectModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectResponseDto extends RepresentationModel<ProjectResponseDto> {
    Integer id;

    String name;

    String description;

    ProjectStatusEnum status;

    UserResponseDto project_owner;

    Set<UserResponseDto> members;

    Long task_count = 0L;

    Instant created_at;

    public ProjectResponseDto(Integer id, String name, String description, Long taskCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.task_count = taskCount;
    }

    public ProjectResponseDto(ProjectModel projectModel) {
        BeanUtils.copyProperties(projectModel, this, "project_owner", "members");

        this.project_owner = new UserResponseDto(projectModel.getProject_owner());

        this.members = projectModel.getMembers().stream()
            .map(UserResponseDto::new)
            .collect(Collectors.toSet());
    }
}

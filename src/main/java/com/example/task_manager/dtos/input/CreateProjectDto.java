package com.example.task_manager.dtos.input;

import org.springframework.beans.BeanUtils;

import com.example.task_manager.models.ProjectModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectDto {
    String name;

    String description;

    Integer project_owner_id;

    public CreateProjectDto(ProjectModel projectModel) {
        BeanUtils.copyProperties(projectModel, this);
    }
}

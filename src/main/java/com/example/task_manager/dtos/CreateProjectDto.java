package com.example.task_manager.dtos;

import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Set;

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

package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.ProjectStatusEnum;
import com.example.task_manager.models.ProjectModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskProjectResponseDto extends RepresentationModel<TaskProjectResponseDto> {
    Integer id;

    String name;

    ProjectStatusEnum status;

    Long task_count = 0L;

    public TaskProjectResponseDto (ProjectModel projectModel) {
        BeanUtils.copyProperties(projectModel, this, "project_owner", "members");
    }
}

package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.models.LabelModel;
import com.example.task_manager.models.ProjectModel;
import com.example.task_manager.models.TaskModel;
import com.example.task_manager.models.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class TaskResponseDto extends RepresentationModel<TaskResponseDto> {

    private Integer id;

    private String title;

    private String description;

    private ProjectModel project;

    private UserModel user;

    private TaskStatusEnum status;

    private TaskPriorityEnum priority;

    private Instant due_date;

    private Instant completion_date;

    private boolean archived;

    private Set<LabelModel> labels;

    public TaskResponseDto(TaskModel aTask) { BeanUtils.copyProperties(aTask, this); }

}

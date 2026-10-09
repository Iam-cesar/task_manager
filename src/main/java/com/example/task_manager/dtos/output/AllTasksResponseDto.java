package com.example.task_manager.dtos.output;

import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;
import com.example.task_manager.models.TaskModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AllTasksResponseDto extends RepresentationModel<AllTasksResponseDto> {

    private Integer id;

    private String title;

    private String description;

    private TaskStatusEnum status;

    private TaskPriorityEnum priority;

    private Instant due_date;

    private Instant completion_date;

    private boolean archived;

    private boolean overdue;

    private List<LabelResponseDto> labels;

    public AllTasksResponseDto (TaskModel taskModel) {

		BeanUtils.copyProperties(taskModel, this, "user", "project", "labels");
        this.overdue = taskModel.isOverdue();
	}
}

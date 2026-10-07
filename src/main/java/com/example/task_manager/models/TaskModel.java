package com.example.task_manager.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.task_manager.dtos.input.CreateTaskDto;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Entity
@NoArgsConstructor
@Table(
    name = "TB_TASKS",
    indexes = {
        @Index(name = "idx_tasks_project", columnList = "project_id"),
        @Index(name = "idx_tasks_user",    columnList = "user_id"),
        @Index(name = "idx_tasks_status",  columnList = "status"),
        @Index(name = "idx_tasks_due_date", columnList = "due_date")
    }
)
public class TaskModel extends BaseEntity {

	public TaskModel(ProjectMemberModel pm) { BeanUtils.copyProperties(pm, this); }

	public TaskModel(
		CreateTaskDto aDto,
		ProjectModel pm,
		UserModel user,
		Set<LabelModel> labels
	) {
		BeanUtils.copyProperties(aDto, this);

		this.project = pm;
		this.user = user;
		labels.forEach(this::addLabel);
	}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_seq")
    @SequenceGenerator(
        name = "tasks_seq",
        sequenceName = "tasks_seq"
    )
    private Integer id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    @Setter
    private String title;

    @Size(max = 255)
    @Column(length = 255)
    @Setter
    private String description;

    @NotNull
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tasks_project"))
    private ProjectModel project;

    @NotNull
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tasks_user")
    )
    private UserModel user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatusEnum status = TaskStatusEnum.PENDING;

    @NotNull
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriorityEnum priority = TaskPriorityEnum.MEDIUM;

    @Setter
    private Instant due_date;

    private Instant completion_date;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean archived = false;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskLabelModel> taskLabels = new HashSet<>();

    public void start() {
        this.status = TaskStatusEnum.RUNNING;
        this.completion_date = null;
    }

    public void complete() {
        this.status = TaskStatusEnum.COMPLETED;
        this.completion_date = Instant.now();
    }

    public void cancel() {
        this.status = TaskStatusEnum.CANCELED;
        this.completion_date = null;
    }

    public void reopen() {
        this.status = TaskStatusEnum.PENDING;
        this.completion_date = null;
    }

    public void changeStatus(TaskStatusEnum newStatus) {
        Objects.requireNonNull(newStatus, "Status cannot be null");
        switch (newStatus) {
            case PENDING   -> reopen();
            case RUNNING   -> start();
            case CANCELED  -> cancel();
            case COMPLETED -> complete();
        }
    }

    public void archive() {
        this.archived = true;
    }

    public void unarchive() {
        this.archived = false;
    }

    public boolean isOverdue() {
        return due_date != null
            && status != TaskStatusEnum.COMPLETED
            && status != TaskStatusEnum.CANCELED
            && due_date.isBefore(Instant.now());            
    }

    public void addLabel(LabelModel label) {
        if (taskLabels.stream().noneMatch(taskLabel -> sameLabel(taskLabel.getLabel(), label))) {
            TaskLabelModel taskLabel = new TaskLabelModel(this, label);
            taskLabels.add(taskLabel);
            label.internalTaskLabels().add(taskLabel);
        }
    }

    public void removeLabel(LabelModel label) {
        taskLabels.removeIf(taskLabel -> {
            if (sameLabel(taskLabel.getLabel(), label)) {
                taskLabel.getLabel().internalTaskLabels().remove(taskLabel);
                return true;
            }
            return false;
        });
    }

    private boolean sameLabel(LabelModel first, LabelModel second) {
        if (first == second) {
            return true;
        }

        return first != null && second != null
            && first.getId() != null && first.getId().equals(second.getId());
    }

    @PreRemove
    public void clearLabels() {
        for (LabelModel label : new HashSet<>(getLabels())) {
            removeLabel(label);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TaskModel taskModel = (TaskModel) o;
        return Objects.equals(id, taskModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Set<LabelModel> getLabels() {
        return taskLabels.stream()
            .map(TaskLabelModel::getLabel)
            .collect(Collectors.toUnmodifiableSet());
    }
}

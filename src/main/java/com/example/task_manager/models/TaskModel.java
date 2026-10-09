package com.example.task_manager.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.task_manager.dtos.input.CreateTaskDto;
import com.example.task_manager.exceptions.InvalidTaskTransitionException;
import com.example.task_manager.exceptions.TaskNotEditableException;
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
import org.jspecify.annotations.NonNull;
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
		@NonNull Set<LabelModel> labels
	) {
		this.title = aDto.title();
		this.description = aDto.description();
		this.due_date = aDto.due_date();
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
    private String title;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tasks_project"))
    private ProjectModel project;

    @NotNull
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriorityEnum priority = TaskPriorityEnum.MEDIUM;

    private Instant due_date;

    private Instant completion_date;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean archived = false;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskLabelModel> taskLabels = new HashSet<>();

    public void setTitle(String title) {
        ensureEditable();
        this.title = title;
    }

    public void setDescription(String description) {
        ensureEditable();
        this.description = description;
    }

    public void setProject(ProjectModel project) {
        ensureEditable();
        this.project = project;
    }

    public void setUser(UserModel user) {
        ensureEditable();
        this.user = user;
    }

    public void setPriority(TaskPriorityEnum priority) {
        ensureEditable();
        this.priority = priority;
    }

    public void setDue_date(Instant dueDate) {
        ensureEditable();
        this.due_date = dueDate;
    }

    public void start() {
        changeStatus(TaskStatusEnum.RUNNING);
    }

    public void complete() {
        changeStatus(TaskStatusEnum.COMPLETED);
    }

    public void cancel() {
        changeStatus(TaskStatusEnum.CANCELED);
    }

    public void reopen() {
        changeStatus(TaskStatusEnum.PENDING);
    }

    public void changeStatus(TaskStatusEnum newStatus) {
        Objects.requireNonNull(newStatus, "Status cannot be null");
        ensureEditable();

        boolean validTransition = switch (status) {
            case PENDING -> newStatus == TaskStatusEnum.RUNNING;
            case RUNNING -> newStatus == TaskStatusEnum.COMPLETED
	                     || newStatus == TaskStatusEnum.CANCELED;
            case COMPLETED, CANCELED -> false;
        };

        if (!validTransition) {
            throw new InvalidTaskTransitionException(
                "Cannot change task status from " + status + " to " + newStatus
            );
        }

        this.status = newStatus;
        this.completion_date = newStatus == TaskStatusEnum.COMPLETED ? Instant.now() : null;
    }

    public void archive() {
        if (archived) {
            throw new TaskNotEditableException("Archived tasks cannot be changed");
        }
        this.archived = true;
    }

    public void ensureEditable() {
        if (archived) {
            throw new TaskNotEditableException("Archived tasks cannot be changed");
        }

        if (status == TaskStatusEnum.COMPLETED || status == TaskStatusEnum.CANCELED) {
            throw new TaskNotEditableException("Completed or canceled tasks cannot be changed");
        }
    }

    public boolean isOverdue() {
        return !archived
            && due_date != null
            && status != TaskStatusEnum.COMPLETED
            && status != TaskStatusEnum.CANCELED
            && due_date.isBefore(Instant.now());            
    }

    public void addLabel(LabelModel label) {
        ensureEditable();
        if (taskLabels.stream().noneMatch(taskLabel -> sameLabel(taskLabel.getLabel(), label))) {
            TaskLabelModel taskLabel = new TaskLabelModel(this, label);
            taskLabels.add(taskLabel);
            label.internalTaskLabels().add(taskLabel);
        }
    }

    public void removeLabel(LabelModel label) {
        ensureEditable();
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
        for (TaskLabelModel taskLabel : new HashSet<>(taskLabels)) {
            taskLabel.getLabel().internalTaskLabels().remove(taskLabel);
        }
        taskLabels.clear();
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

    public Set<TaskLabelModel> getTaskLabels() {
        return Set.copyOf(taskLabels);
    }
}

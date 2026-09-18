package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.TaskPriorityEnum;
import com.example.task_manager.enums.TaskStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "TB_TASKS",
        indexes = {
                @Index(name = "idx_tasks_project", columnList = "project_id"),
                @Index(name = "idx_tasks_user",    columnList = "user_id"),
                @Index(name = "idx_tasks_status",  columnList = "status"),
                @Index(name = "idx_tasks_due_date", columnList = "dueDate")
        }
)
public class TaskModel extends BaseEntity {
    @Setter
    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "tasks_seq")
    @SequenceGenerator(
            name = "tasks_seq",
            sequenceName = "tasks_seq",
            allocationSize = 50
    )
    private Long id;

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
    private LocalDateTime dueDate;

    private Instant completionDate;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean archived = false;

    @ManyToMany
    @JoinTable(
            name = "TB_TASKS_LABELS",
            joinColumns = @JoinColumn(
                    name = "task_id",
                    foreignKey = @ForeignKey(name = "fk_tasks_labels_task")),
            inverseJoinColumns = @JoinColumn(
                    name = "label_id",
                    foreignKey = @ForeignKey(name = "fk_tasks_labels_label"))
    )
    private Set<LabelModel> labels = new HashSet<>();

    public void start() {
        this.status = TaskStatusEnum.RUNNING;
        this.completionDate = null;
    }

    public void complete() {
        this.status = TaskStatusEnum.COMPLETED;
        this.completionDate = Instant.now();
    }

    public void cancel() {
        this.status = TaskStatusEnum.CANCELED;
        this.completionDate = null;
    }

    public void reopen() {
        this.status = TaskStatusEnum.PENDING;
        this.completionDate = null;
    }

    public void changeStatus(TaskStatusEnum newStatus) {
        Objects.requireNonNull(newStatus, "status nao pode ser null");
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
        return dueDate != null
                && status != TaskStatusEnum.COMPLETED
                && status != TaskStatusEnum.CANCELED
                && dueDate.isBefore(LocalDateTime.now());
    }

    public void addLabel(LabelModel label) {
        this.labels.add(label);
        label.internalTasks().add(this);
    }

    public void removeLabel(LabelModel label) {
        this.labels.remove(label);
        label.internalTasks().remove(this);
    }

    public void clearLabels() {
        for (LabelModel label : new HashSet<>(this.labels)) {
            removeLabel(label);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        TaskModel that = (TaskModel) o;
        return id != null && Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Set<LabelModel> getLabels() {
        return Collections.unmodifiableSet(labels);
    }
}

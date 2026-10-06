package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(
        name = "TB_TASK_LABELS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_task_labels_task_label",
                columnNames = {"task_id", "label_id"}
        )
)
public class TaskLabelModel extends BaseEntity {

    public TaskLabelModel(TaskModel aTask, LabelModel aLabel) {
        this.task = aTask;
        this.label = aLabel;
    }

    @Id
    @Getter
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "task_label_seq"
    )
    @SequenceGenerator(
            name = "task_label_seq",
            sequenceName = "task_label_seq"
    )
    private Integer id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_task_labels_task"
            )
    )
    private TaskModel task;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "label_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_task_labels_label"
            )
    )
    private LabelModel label;
}

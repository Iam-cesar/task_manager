package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(
    name = "TB_PROJECT_LABELS",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_project_labels_project_label",
        columnNames = {"project_id", "label_id"}
    )
)
public class ProjectLabelModel extends BaseEntity {

    public ProjectLabelModel(ProjectModel aProject, LabelModel aLabel) {
        this.project = aProject;
        this.label = aLabel;
    }

    @Id
    @Getter
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "project_label_seq"
    )
    @SequenceGenerator(
        name = "project_label_seq",
        sequenceName = "project_label_seq"
    )
    private Integer id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "project_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_project_labels_project"
        )
    )
    private ProjectModel project;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "label_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_project_labels_label"
        )
    )
    private LabelModel label;
}

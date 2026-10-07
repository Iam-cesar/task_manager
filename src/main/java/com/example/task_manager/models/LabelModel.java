package com.example.task_manager.models;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.task_manager.dtos.input.CreateLabelDto;
import com.example.task_manager.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "TB_LABELS")
public class LabelModel extends BaseEntity {

	public LabelModel(CreateLabelDto aDto) {
		this.name = aDto.name();
		this.color = aDto.color();
	}

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "labels_seq")
    @SequenceGenerator(
        name = "labels_seq",
        sequenceName = "labels_seq"
    )
    private Integer id;

    @Setter
    @Getter
    @NotBlank
    @Size(max = 60)
    @Column(
        nullable = false,
        unique = true,
        length = 60
    )
    private String name;

    @Setter
    @Getter
    @NotBlank
    @Column(nullable = false, length = 20)
    @Size(max = 20)
    private String color = "#B0C4DE";

    @OneToMany(mappedBy = "label")
    private Set<TaskLabelModel> taskLabels = new HashSet<>();

    @PreRemove
    private void removeLabelFromTasks() {
        for (TaskLabelModel taskLabel : new HashSet<>(taskLabels)) {
            taskLabel.getTask().removeLabel(this);
        }
    }

    Set<TaskLabelModel> internalTaskLabels() {
        return taskLabels;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LabelModel that = (LabelModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Set<TaskModel> getTasks() {
        return taskLabels.stream()
            .map(TaskLabelModel::getTask)
            .collect(Collectors.toUnmodifiableSet());
    }
}

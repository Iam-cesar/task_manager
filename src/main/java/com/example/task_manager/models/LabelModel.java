package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@NoArgsConstructor
@Table(name = "TB_LABELS")
public class LabelModel extends BaseEntity {
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
    private String color;

    @ManyToMany(mappedBy = "labels")
    private Set<TaskModel> tasks = new HashSet<>();

    @PreRemove
    private void removeLabelFromTasks() {
        for (TaskModel task : new HashSet<>(tasks)) {
            task.removeLabel(this);
        }
    }

    Set<TaskModel> internalTasks() {
        return tasks;
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
        return Collections.unmodifiableSet(tasks);
    }
}

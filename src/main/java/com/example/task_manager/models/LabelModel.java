package com.example.task_manager.models;

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
public class LabelModel {
    @Getter
    @Setter
    @Version
    private Long version;


    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "labels_seq")
    @SequenceGenerator(
            name = "labels_seq",
            sequenceName = "labels_seq",
            allocationSize = 50
    )
    private Long id;

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
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        LabelModel that = (LabelModel) o;
        return id != null && Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Set<TaskModel> getTasks() {
        return Collections.unmodifiableSet(tasks);
    }
}

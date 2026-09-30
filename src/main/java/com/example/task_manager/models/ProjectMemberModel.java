package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "TB_PROJECT_MEMBERS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_members_project_user",
                columnNames = {"project_id", "user_id"}
        )
)
public class ProjectMemberModel extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "project_members_seq"
    )
    @SequenceGenerator(
            name = "project_members_seq",
            sequenceName = "project_members_seq"
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            foreignKey = @ForeignKey(
                    name = "fk_project_members_project"
            )
    )
    private ProjectModel project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(
                    name = "fk_project_members_user"
            )
    )
    private UserModel user;

    public ProjectMemberModel(ProjectModel project, UserModel user) {
        this.project = project;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMemberModel that = (ProjectMemberModel) o;
        return Objects.equals(id, that.id) && Objects.equals(project, that.project) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, project, user);
    }
}
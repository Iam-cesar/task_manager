package com.example.task_manager.models;

import java.util.Objects;

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
    name = "TB_PROJECT_MEMBERS",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_project_members_project_user",
        columnNames = {"project_id", "user_id"}
    )
)
public class ProjectMemberModel extends BaseEntity {

    public ProjectMemberModel(ProjectModel aProject, UserModel anUser) {
        this.project = aProject;
        this.user = anUser;
    }

    @Id
    @Getter
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "project_members_seq"
    )
    @SequenceGenerator(
        name = "project_members_seq",
        sequenceName = "project_members_seq"
    )
    private Integer id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "project_id",
        foreignKey = @ForeignKey(   
            name = "fk_project_members_project"
        )
    )
    private ProjectModel project;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",   
        foreignKey = @ForeignKey(   
            name = "fk_project_members_user"    
        )   
    )
    private UserModel user;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMemberModel that = (ProjectMemberModel) o;
        return Objects.equals(id, that.id) && Objects.equals(project, that.project) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() { return Objects.hash(id, project, user); }
}

package com.example.task_manager.models;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import com.example.task_manager.dtos.input.CreateProjectDto;
import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.ProjectStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "TB_PROJECTS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_projects_owner_name",
                columnNames = {"project_owner_id", "name"}
        ),
        indexes = {
                @Index(name = "idx_projects_status", columnList = "status")
        }
)
public class ProjectModel extends BaseEntity {

    public ProjectModel (CreateProjectDto createProjectDto) {
        BeanUtils.copyProperties(createProjectDto, this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_seq")
    @SequenceGenerator(
            name = "projects_seq",
            sequenceName = "projects_seq"
    )
    private Integer id;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectModel that = (ProjectModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @NotBlank
    @Size(max = 60, min = 3)
    @Column(nullable = false, length = 60)
    @Setter
    private String name;

    @Setter
    @Size(max = 255)
    @Column()
    private String description;

    @Setter
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_projects_project_owner")
    )
    private UserModel project_owner;

    @Setter
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "TB_PROJECT_MEMBERS",
            joinColumns = @JoinColumn(
                    name = "project_id",
                    foreignKey = @ForeignKey(name = "fk_projects_members_project")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_projects_members_user")
            )
    )
    private Set<UserModel> members = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatusEnum status = ProjectStatusEnum.ACTIVE;

    public void activate() {
        this.status = ProjectStatusEnum.ACTIVE;
    }

    public void deactivate() {
        this.status = ProjectStatusEnum.INACTIVE;
    }

    public boolean isActive() {
        return this.status == ProjectStatusEnum.ACTIVE;
    }
}

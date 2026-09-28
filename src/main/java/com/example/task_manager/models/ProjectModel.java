package com.example.task_manager.models;

import com.example.task_manager.dtos.CreateProjectDto;
import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.ProjectStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    @Setter
    private String name;

    @Setter
    @Size(max = 255)
    @Column(length = 255)
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TB_PROJECTS_MEMBERS",
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

    public void addMember(UserModel member) { members.add(member); }
    public void removeMember(UserModel member) { members.remove(member); }

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

package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.ProjectStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@Table(
        name = "TB_PROJECTS",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_projects_user_name",
                columnNames = {"user_id", "name"}
        ),
        indexes = {
                @Index(name = "idx_projects_status", columnList = "status")
        }
)
public class ProjectModel extends BaseEntity {
    @Setter
    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "projects_seq")
    @SequenceGenerator(
            name = "projects_seq",
            sequenceName = "projects_seq",
            allocationSize = 50
    )
    private Long id;

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
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_projects_user")
    )
    private UserModel user;

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

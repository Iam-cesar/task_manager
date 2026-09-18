package com.example.task_manager.models;

import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.UserStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USERS")
public class UserModel extends BaseEntity {
    @Setter
    @Version
    private Long version;

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "users_seq")
    @SequenceGenerator(
            name = "users_seq",
            sequenceName = "users_seq",
            allocationSize = 50
    )
    private Long id;

    @Setter
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    @Setter
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatusEnum status = UserStatusEnum.ACTIVE;

    public void activate() {
        this.status = UserStatusEnum.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatusEnum.INACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatusEnum.ACTIVE;
    }
}

package com.example.task_manager.models;

import java.util.Objects;

import org.springframework.beans.BeanUtils;

import com.example.task_manager.dtos.output.UserResponseDto;
import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.UserStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USERS")
public class UserModel extends BaseEntity {

    public UserModel(UserResponseDto aDto) {
        BeanUtils.copyProperties(aDto, this);
    }

    public UserModel(ProjectMemberModel p) {
        BeanUtils.copyProperties(p, this);
    }

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(
        name = "users_seq",
        sequenceName = "users_seq"
    )
    private Integer id;

    @Setter
    @NotBlank
    @Size(max = 60, min = 3)
    @Column(nullable = false, length = 60)
    private String name;

    @Setter
    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(id, userModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

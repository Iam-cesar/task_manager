package com.example.task_manager.models;

import com.example.task_manager.dtos.UserResponseDto;
import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.enums.UserStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Locale;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USERS")
public class UserModel extends BaseEntity {

    public UserModel(UserResponseDto userResponseDto) {
        BeanUtils.copyProperties(userResponseDto, this);
    }

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(
            name = "users_seq",
            sequenceName = "users_seq",
            allocationSize = 50
    )
    private Integer id;

    @Setter
    @NotBlank
    @Size(max = 60)
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

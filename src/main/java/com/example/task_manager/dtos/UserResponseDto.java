package com.example.task_manager.dtos;

import com.example.task_manager.entities.BaseEntity;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserResponseDto extends RepresentationModel<UserResponseDto> {

    Integer id;

    String name;

    String email;

    UserStatusEnum status;

    Instant created_at;

    public UserResponseDto(UserModel user) {
        BeanUtils.copyProperties(user, this);
    }
}

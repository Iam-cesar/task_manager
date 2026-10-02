package com.example.task_manager.dtos.output;

import java.time.Instant;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.RepresentationModel;

import com.example.task_manager.enums.UserStatusEnum;
import com.example.task_manager.models.UserModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

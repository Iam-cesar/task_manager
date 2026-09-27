package com.example.task_manager.dtos;

import com.example.task_manager.models.UserModel;
import com.example.task_manager.enums.UserStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
public class UserResponseDto {

    Integer id;

    String name;

    String email;

    UserStatusEnum status;

    public UserResponseDto(UserModel user) {
        BeanUtils.copyProperties(user, this);
    }
}

package com.example.task_manager.dtos.input;

import com.example.task_manager.models.UserModel;
import org.springframework.beans.BeanUtils;

public class CreateUserDto {

	private String name;

	private String email;

	public CreateUserDto(UserModel aUserModel) { BeanUtils.copyProperties(aUserModel, this);	}
}

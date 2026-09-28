package com.example.task_manager.services;

import com.example.task_manager.dtos.UpdateUserDto;
import com.example.task_manager.enums.UserStatusEnum;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserModel saveAndFlush(@NonNull UserModel user) throws UserAlreadyExistsException {

        Optional<UserModel> userServiceByEmail = userRepository.findByEmail(user.getEmail());
        String userNotFoundByEmailMessage = "User with email " + user.getEmail() + " already exists";

        if (userServiceByEmail.isPresent()) {
            throw new UserAlreadyExistsException(userNotFoundByEmailMessage);
        }

        return userRepository.saveAndFlush(user);
    }

    public List<UserModel> findAll() { return userRepository.findAll(); }

    public UserModel findById(int id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public UserModel updateAndFlush(
            int id,
            @NonNull UpdateUserDto updateUserDto
    ) throws UserAlreadyExistsException {

        UserModel userById = findById(id);

        if (existsByEmail(updateUserDto.email())) {
            String userEmailConflictMessage = "Já existe um usuário com esse e-mail";
            throw new UserAlreadyExistsException(userEmailConflictMessage);
        }

        BeanUtils.copyProperties(updateUserDto, userById);

        return userRepository.saveAndFlush(userById);
    }

    public UserModel status(int id) {
        UserModel userById = findById(id);

        return userById.getStatus() ==  UserStatusEnum.ACTIVE
                ? deactivate(userById)
                : activate(userById);
    }

    private @NonNull UserModel deactivate(@NonNull UserModel user)  {
        user.deactivate();

        return userRepository.saveAndFlush(user);
    }

    private @NonNull UserModel activate(@NonNull UserModel user) {
        user.activate();

        return userRepository.saveAndFlush(user);
    }

    public void deleteById(int id) {

        UserModel user = findById(id);

        if (user != null) { userRepository.deleteById(id); }
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean isEmailChanged(@NonNull UpdateUserDto dto, String userByIdEmail) {
        return dto.email() != null && !dto.email().equals(userByIdEmail);
    }
}

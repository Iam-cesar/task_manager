package com.example.task_manager.services;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.example.task_manager.dtos.input.UpdateUserDto;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

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

        return userById.isActive()
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
}

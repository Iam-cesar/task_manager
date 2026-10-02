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

    public UserModel saveAndFlush(@NonNull final UserModel aUser) throws UserAlreadyExistsException {

        final Optional<UserModel> userServiceByEmail = userRepository.findByEmail(aUser.getEmail());
        final String userNotFoundByEmailMessage = "User with email " + aUser.getEmail() + " already exists";

        if (userServiceByEmail.isPresent()) {
            throw new UserAlreadyExistsException(userNotFoundByEmailMessage);
        }

        return userRepository.saveAndFlush(aUser);
    }

    public List<UserModel> findAll() { return userRepository.findAll(); }

    public UserModel findById(final int anId) throws UserNotFoundException {
        return userRepository.findById(anId).orElseThrow(UserNotFoundException::new);
    }

    public UserModel updateAndFlush(
            final int anId,
            @NonNull final UpdateUserDto updateUserDto
    ) throws UserAlreadyExistsException {

        final var userById = findById(anId);

        if (existsByEmail(updateUserDto.email())) {
            final String userEmailConflictMessage = "Já existe um usuário com esse e-mail";
            throw new UserAlreadyExistsException(userEmailConflictMessage);
        }

        BeanUtils.copyProperties(updateUserDto, userById);

        return userRepository.saveAndFlush(userById);
    }

    public UserModel status(final int anId) {
        final var userById = findById(anId);

        return userById.isActive()
                ? deactivate(userById)
                : activate(userById);
    }

    private @NonNull UserModel deactivate(@NonNull final UserModel aUser)  {
        aUser.deactivate();

        return userRepository.saveAndFlush(aUser);
    }

    private @NonNull UserModel activate(@NonNull final UserModel aUser) {
        aUser.activate();

        return userRepository.saveAndFlush(aUser);
    }

    public void deleteById(final int anId) {

        UserModel user = findById(anId);

        if (user != null) { userRepository.deleteById(anId); }
    }

    public boolean existsByEmail(final String anEmail) {
        return userRepository.findByEmail(anEmail).isPresent();
    }
}

package com.example.task_manager.services;

import com.example.task_manager.dtos.UpdateUserDto;
import com.example.task_manager.enums.UserStatusEnum;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final String userNotFoundMessage = "Usuário não encontrado";

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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(userNotFoundMessage));
    }

    public UserModel findByEmail(String email) {

        return userRepository.findByEmail(email).orElse(null);
    }

    public UserModel update(
            int id,
            @NonNull UpdateUserDto dto
    ) throws UserAlreadyExistsException {

        UserModel userById = findById(id);

        if (existsByEmail(dto.email())) {
            String userEmailConflictMessage = "Já existe um usuário com esse e-mail";
            throw new UserAlreadyExistsException(userEmailConflictMessage);
        }

        if (dto.name() != null) { userById.setName(dto.name()); }

        if (isEmailChanged(dto, userById.getEmail())) { userById.setEmail(dto.email()); }

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

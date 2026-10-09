package com.example.task_manager.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.task_manager.dtos.input.UpdateUserDto;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserHasTasksException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.TaskRepository;
import com.example.task_manager.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

	@Transactional
    public UserModel saveAndFlush(@NonNull final UserModel aUser) throws UserAlreadyExistsException {

        final Optional<UserModel> userServiceByEmail = userRepository.findByEmail(aUser.getEmail());
        final String userNotFoundByEmailMessage = "User with email " + aUser.getEmail() + " already exists";

        if (userServiceByEmail.isPresent()) {
            throw new UserAlreadyExistsException(userNotFoundByEmailMessage);
        }

        return userRepository.saveAndFlush(aUser);
    }

	@Transactional(readOnly = true)
    public Page<UserModel> findAll(final String aSearch, Pageable pageable) {

		Page<Number> idsPage = aSearch == null || aSearch.isBlank()
			? userRepository.findAllIds(pageable)
			: userRepository.searchByNameAndEmail(aSearch, pageable);

		var integerIdsPage = idsPage.map(id -> Math.toIntExact(id.longValue()));

		if (integerIdsPage.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, integerIdsPage.getTotalElements());
		}

	    var usersById = userRepository.findAllById(integerIdsPage.getContent())
		    .stream()
		    .collect(Collectors.toMap(UserModel::getId, Function.identity()));

		var usersInPageOrder = integerIdsPage.getContent().stream()
			.map(usersById::get)
			.filter(Objects::nonNull)
			.toList();

	    return new PageImpl<>(usersInPageOrder, pageable, integerIdsPage.getTotalElements());
	}

    public UserModel findById(final int anId) throws UserNotFoundException {
        return userRepository.findById(anId).orElseThrow(UserNotFoundException::new);
    }

	@Transactional
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

        if (taskRepository.existsAssignedTasksForUser(user.getId())) {
            throw new UserHasTasksException();
        }

        userRepository.deleteById(user.getId());
    }

    public boolean existsByEmail(final String anEmail) {
        return userRepository.findByEmail(anEmail).isPresent();
    }
}

package com.example.task_manager.controllers;

import com.example.task_manager.dtos.UpdateUserDto;
import com.example.task_manager.dtos.UserResponseDto;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Transactional
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid @NonNull UserModel user) {

        UserResponseDto userCreated = new UserResponseDto(userService.saveAndFlush(user));

        return ResponseEntity.ok(userCreated);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        List<UserResponseDto> users = convertUsersToList(userService.findAll());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable int id)  {

        UserResponseDto user = new UserResponseDto(userService.findById(id));

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<UserResponseDto> update (
            @PathVariable int id,
            @RequestBody @NonNull UpdateUserDto user
    ) {
        UserResponseDto userUpdated = new UserResponseDto(userService.update(id, user));

        return ResponseEntity.ok(userUpdated);
    }

    @PostMapping("/{id}/status")
    @Transactional
    public ResponseEntity<UserResponseDto> status(@PathVariable int id) {

        UserModel user = userService.status(id);

        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private @NonNull List<UserResponseDto> convertUsersToList (@NonNull List<UserModel> users) {

        return users.stream().map(UserResponseDto::new).toList();
    }
}

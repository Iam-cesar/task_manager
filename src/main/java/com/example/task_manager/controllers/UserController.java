package com.example.task_manager.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.task_manager.dtos.input.UpdateUserDto;
import com.example.task_manager.dtos.output.UserResponseDto;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.services.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
        @RequestBody @Valid @NonNull UserModel aUser,
        @NonNull UriComponentsBuilder uriBuilder
    ) {
        var userCreated = new UserResponseDto(userService.saveAndFlush(aUser));
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(userCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(userCreated);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {

        List<UserResponseDto> users = convertUsersToList(userService.findAll());

        if  (!users.isEmpty()) {
            for (UserResponseDto user : users) {
                int id  = user.getId();
                user.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable int id)  {

        var user = new UserResponseDto(userService.findById(id));
        user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser (
        @PathVariable int id,
        @RequestBody @NonNull UpdateUserDto aUser
    ) {
        var userUpdated = new UserResponseDto(userService.updateAndFlush(id, aUser));

        return ResponseEntity.ok(userUpdated);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> status(@PathVariable int id) {

        UserModel user = userService.status(id);

        return ResponseEntity.ok(new UserResponseDto(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private @NonNull List<UserResponseDto> convertUsersToList (@NonNull final List<UserModel> alistOfUserModels) {

        return alistOfUserModels.stream().map(UserResponseDto::new).toList();
    }
}

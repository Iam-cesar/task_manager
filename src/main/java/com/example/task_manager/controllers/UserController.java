package com.example.task_manager.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;

import com.example.task_manager.helpers.ConvertRepresentationModel;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        final var userCreated = new UserResponseDto(userService.saveAndFlush(aUser));
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(userCreated.getId()).toUri();

        return ResponseEntity.created(uri).body(userCreated);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
	    @RequestParam(required = false) String search
    ) {

        final List<UserResponseDto> users = ConvertRepresentationModel
	        .toList(userService.findAll(search),  UserResponseDto::new);

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

        final var user = new UserResponseDto(userService.findById(id));
        user.add(linkTo(methodOn(UserController.class).getAllUsers(null)).withRel("users"));

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser (
        @PathVariable int id,
        @RequestBody @NonNull UpdateUserDto aUser
    ) {
        final var userUpdated = new UserResponseDto(userService.updateAndFlush(id, aUser));

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
}

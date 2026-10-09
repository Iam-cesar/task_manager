package com.example.task_manager.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;

import com.example.task_manager.helpers.ConvertRepresentationModel;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.task_manager.dtos.input.UpdateUserDto;
import com.example.task_manager.dtos.output.UserResponseDto;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.services.UserService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "Manage users and their active status")
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
    public ResponseEntity<PagedModel<UserResponseDto>> getAllUsers(
	    @RequestParam(required = false) String search,
	    @PageableDefault(size = 10) final Pageable pageable

    ) {
        final Page<UserResponseDto> users = ConvertRepresentationModel
	        .toPageList(userService.findAll(search, pageable),  UserResponseDto::new);

        if  (!users.isEmpty()) {
            for (UserResponseDto user : users.getContent()) {
                int id  = user.getId();
                user.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
            }
        }

        return ResponseEntity.ok(new PagedModel<>(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable int id)  {

        final var user = new UserResponseDto(userService.findById(id));
        user.add(linkTo(methodOn(UserController.class).getAllUsers(null, null)).withRel("users"));

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

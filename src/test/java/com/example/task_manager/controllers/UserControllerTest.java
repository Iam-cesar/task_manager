package com.example.task_manager.controllers;

import com.example.task_manager.dtos.UpdateUserDto;
import com.example.task_manager.dtos.UserResponseDto;
import com.example.task_manager.enums.UserStatusEnum;
import com.example.task_manager.exceptions.UserAlreadyExistsException;
import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserModel createMockUser(Integer id, String name, String email) {
        UserModel user = new UserModel();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    @Nested
    @DisplayName("POST /users")
    class CreateUserTests {

        @Test
        @DisplayName("Should return 201 Created with Location header and user body")
        void shouldCreateUserSuccessfully() throws Exception {
            UserModel savedUser = createMockUser(1, "John Doe", "john@example.com");
            when(userService.saveAndFlush(any(UserModel.class))).thenReturn(savedUser);

            String requestBody = """
                {
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/users/1")))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("John Doe")))
                    .andExpect(jsonPath("$.email", is("john@example.com")))
                    .andExpect(jsonPath("$.status", is("active")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when payload is invalid")
        void shouldReturn400WhenPayloadIsInvalid() throws Exception {
            String invalidRequestBody = """
                {
                    "name": "",
                    "email": "not-an-email"
                }
                """;

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 Conflict when email already exists")
        void shouldReturn409WhenUserAlreadyExists() throws Exception {
            when(userService.saveAndFlush(any(UserModel.class)))
                    .thenThrow(new UserAlreadyExistsException("Já existe um usuário com esse e-mail"));

            String requestBody = """
                {
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is("Já existe um usuário com esse e-mail")));
        }
    }

    @Nested
    @DisplayName("GET /users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return 200 OK with list of users and HATEOAS self links")
        void shouldReturnAllUsersWithHateoasLinks() throws Exception {
            UserModel user1 = createMockUser(1, "User One", "user1@example.com");
            UserModel user2 = createMockUser(2, "User Two", "user2@example.com");
            when(userService.findAll()).thenReturn(List.of(user1, user2));

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("User One")))
                    .andExpect(jsonPath("$[0].links[0].rel", is("self")))
                    .andExpect(jsonPath("$[0].links[0].href", containsString("/users/1")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("User Two")))
                    .andExpect(jsonPath("$[1].links[0].rel", is("self")))
                    .andExpect(jsonPath("$[1].links[0].href", containsString("/users/2")));
        }

        @Test
        @DisplayName("Should return 200 OK with empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() throws Exception {
            when(userService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /users/{id}")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return 200 OK with user and HATEOAS link to all users")
        void shouldReturnUserByIdWithHateoasLink() throws Exception {
            UserModel user = createMockUser(1, "John Doe", "john@example.com");
            when(userService.findById(1)).thenReturn(user);

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("John Doe")))
                    .andExpect(jsonPath("$.email", is("john@example.com")))
                    .andExpect(jsonPath("$._links.self.href", containsString("/users")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when user does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.findById(999)).thenThrow(new UserNotFoundException("User not found"));

            mockMvc.perform(get("/users/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", is("User not found")));
        }
    }

    @Nested
    @DisplayName("PATCH /users/{id}")
    class UpdateUserTests {

        @Test
        @DisplayName("Should return 200 OK with updated user")
        void shouldUpdateUserSuccessfully() throws Exception {
            UserModel updatedUser = createMockUser(1, "Updated Name", "updated@example.com");
            when(userService.updateAndFlush(eq(1), any(UpdateUserDto.class))).thenReturn(updatedUser);

            String requestBody = """
                {
                    "name": "Updated Name",
                    "email": "updated@example.com"
                }
                """;

            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated Name")))
                    .andExpect(jsonPath("$.email", is("updated@example.com")));
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existing user")
        void shouldReturn404WhenUpdatingNonExistingUser() throws Exception {
            when(userService.updateAndFlush(eq(999), any(UpdateUserDto.class)))
                    .thenThrow(new UserNotFoundException("User not found"));

            String requestBody = """
                {
                    "name": "Updated Name"
                }
                """;

            mockMvc.perform(patch("/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 Conflict when updating to an existing email")
        void shouldReturn409WhenUpdatingToExistingEmail() throws Exception {
            when(userService.updateAndFlush(eq(1), any(UpdateUserDto.class)))
                    .thenThrow(new UserAlreadyExistsException("Já existe um usuário com esse e-mail"));

            String requestBody = """
                {
                    "email": "conflict@example.com"
                }
                """;

            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", is("Já existe um usuário com esse e-mail")));
        }
    }

    @Nested
    @DisplayName("POST /users/{id}/status")
    class ToggleStatusTests {

        @Test
        @DisplayName("Should return 200 OK with toggled user status")
        void shouldToggleUserStatus() throws Exception {
            UserModel user = createMockUser(1, "John Doe", "john@example.com");
            user.deactivate();
            when(userService.status(1)).thenReturn(user);

            mockMvc.perform(post("/users/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.status", is("inactive")));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class DeleteUserTests {

        @Test
        @DisplayName("Should return 204 No Content when deleting existing user")
        void shouldDeleteUserSuccessfully() throws Exception {
            doNothing().when(userService).deleteById(1);

            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existing user")
        void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
            doThrow(new UserNotFoundException("User not found")).when(userService).deleteById(999);

            mockMvc.perform(delete("/users/999"))
                    .andExpect(status().isNotFound());
        }
    }
}

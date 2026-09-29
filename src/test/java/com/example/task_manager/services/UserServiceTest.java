package com.example.task_manager.services;

import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete user when user exists")
        void shouldDeleteUserWhenUserExists() {
            UserModel user = new UserModel();
            ReflectionTestUtils.setField(user, "id", 1);
            user.setName("John Doe");
            user.setEmail("john@example.com");

            when(userRepository.findById(1)).thenReturn(Optional.of(user));

            assertDoesNotThrow(() -> userService.deleteById(1));

            verify(userRepository).findById(1);
            verify(userRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.deleteById(999));

            verify(userRepository).findById(999);
        }
    }
}

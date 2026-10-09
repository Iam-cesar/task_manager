package com.example.task_manager.services;

import com.example.task_manager.exceptions.UserNotFoundException;
import com.example.task_manager.exceptions.UserHasTasksException;
import com.example.task_manager.models.UserModel;
import com.example.task_manager.repositories.TaskRepository;
import com.example.task_manager.repositories.UserRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Should return users in the same order as the paged IDs")
        void shouldReturnPageOfUsersInIdPageOrder() {
            var pageable = PageRequest.of(0, 2);
            var first = new UserModel();
            ReflectionTestUtils.setField(first, "id", 1);
            first.setName("A User");
            first.setEmail("a@example.com");
            var second = new UserModel();
            ReflectionTestUtils.setField(second, "id", 2);
            second.setName("B User");
            second.setEmail("b@example.com");

            when(userRepository.findAllIds(pageable))
                .thenReturn(new PageImpl<>(List.of(1, 2), pageable, 3));
            when(userRepository.findAllById(List.of(1, 2)))
                .thenReturn(List.of(second, first));

            var result = userService.findAll(null, pageable);

            assertEquals(List.of(first, second), result.getContent());
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("Should convert native query Long IDs to Integer IDs")
        void shouldConvertLongIdsFromSearchResults() {
            var pageable = PageRequest.of(0, 1);
            var user = new UserModel();
            ReflectionTestUtils.setField(user, "id", 42);
            user.setName("A User");
            user.setEmail("a@example.com");

            when(userRepository.searchByNameAndEmail("A User", pageable))
                .thenReturn(new PageImpl<>(List.of(42L), pageable, 1));
            when(userRepository.findAllById(List.of(42)))
                .thenReturn(List.of(user));

            var result = userService.findAll("A User", pageable);

            assertEquals(List.of(user), result.getContent());
            verify(userRepository).findAllById(List.of(42));
        }
    }

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
            when(taskRepository.existsAssignedTasksForUser(1)).thenReturn(false);

            assertDoesNotThrow(() -> userService.deleteById(1));

            verify(userRepository).findById(1);
            verify(taskRepository).existsAssignedTasksForUser(1);
            verify(userRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should block deletion when the user has assigned tasks")
        void shouldBlockDeletingUserWithAssignedTasks() {
            var user = new UserModel();
            ReflectionTestUtils.setField(user, "id", 1);
            when(userRepository.findById(1)).thenReturn(Optional.of(user));
            when(taskRepository.existsAssignedTasksForUser(1)).thenReturn(true);

            assertThrows(UserHasTasksException.class, () -> userService.deleteById(1));

            verify(userRepository, never()).deleteById(1);
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

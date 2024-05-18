package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    private static UserController userController;

    @BeforeEach
    void init() {
        userController = new UserController();
    }

    @Test
    void validateUserOk() {
        final User user = User.builder()
                .id(1L)
                .email("test@test.ru")
                .login("Login")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        userController.validate(user);
    }

    @Test
    void validateUserFailIfMailEmpty() {
        final User user = User.builder()
                .id(1L)
                .login("Login")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void validateUserFailIsMailNotContainsDogSymbol() {
        final User user = User.builder()
                .id(1L)
                .email("testtest.ru")
                .login("Login")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void validateUserFailIfLoginEmpty() {
        final User user = User.builder()
                .id(1L)
                .email("test@test.ru")
                .name("name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void validateUserFailIfLoginBlank() {
        final User user = User.builder()
                .id(1L)
                .email("test@test.ru")
                .name("name")
                .login("       ")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userController.validate(user));
    }

    @Test
    void validateUserFailIfBirthdayInFuture() {
        final User user = User.builder()
                .id(1L)
                .email("test@test.ru")
                .login("Login")
                .name("name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> userController.validate(user));
    }
}

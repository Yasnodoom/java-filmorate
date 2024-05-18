package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.StorageData;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceTest {
    private static Storage<User> userStorage;
    private static UserService userService;

    private User validUser;
    private User friend;

    @BeforeEach
    void init() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        validUser = new User(
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );
        validUser.setId(1L);

        // without id
        friend = new User(
                "test@test.ru",
                "LoginFriend",
                "Andy",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        userStorage.create(validUser);
    }

    @Test
    void validateUserOk() {
        userService.validate(validUser);
    }

    @Test
    void validateUserFailIfMailEmpty() {
        validUser.setEmail("");
        assertThrows(ValidationException.class, () -> userService.validate(validUser));
    }

    @Test
    void validateUserFailIsMailNotContainsDogSymbol() {
        validUser.setEmail("testtest.ru");
        assertThrows(ValidationException.class, () -> userService.validate(validUser));
    }

    @Test
    void validateUserFailIfLoginEmpty() {
        validUser.setLogin("");
        assertThrows(ValidationException.class, () -> userService.validate(validUser));
    }

    @Test
    void validateUserFailIfLoginBlank() {
        validUser.setLogin("  ");
        assertThrows(ValidationException.class, () -> userService.validate(validUser));
    }

    @Test
    void validateUserFailIfBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userService.validate(validUser));
    }

    @Test
    void successfulAddFriend() {
        final long friendId = 2L;
        friend.setId(friendId);

        userStorage.create(friend);
        userService.addFriend(validUser.getId(), friend.getId());
        assertEquals(validUser.getFriends(), Set.of(friendId));
        assertEquals(friend.getFriends(), Set.of(validUser.getId()));
    }

    @Test
    void validateErrorWhenAddFriendAndFriendIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.addFriend(userId, 3L));
    }

    @Test
    void validateErrorWhenAddFriendAndUserIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.addFriend(3L, userId));
    }

    @Test
    void successfulDeleteFriend() {
        successfulAddFriend();
        final long friendId = userStorage.getAll().stream()
                .map(StorageData::getId)
                .filter(id -> !id.equals(validUser.getId()))
                .findFirst()
                .get();

        userService.deleteFriend(validUser.getId(), friendId);
        assertEquals(Collections.EMPTY_SET, validUser.getFriends());
    }

    @Test
    void validateErrorWhenDeleteFriendAndFriendIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(userId, 3L));
    }

    @Test
    void validateErrorWhenDeleteFriendAndUserIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(3L, userId));
    }

    @Test
    void successfulGetCommonFriend() {
        successfulAddFriend();

        User user3 = new User(
                "test@test.ru",
                "Garage",
                "Max",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        final long user3Id = 3L;
        user3.setId(user3Id);
        userStorage.create(user3);
        final long user2Id = friend.getId();

        userService.addFriend(validUser.getId(), user3Id);
        userService.addFriend(user2Id, user3Id);

        assertEquals(userService.getCommonFriends(user2Id, validUser.getId()), Set.of(user3));
    }

    @Test
    void validateErrorWhenGetCommonFriendAndFriendIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(userId, 3L));
    }

    @Test
    void validateErrorWhenGetCommonFriendAndUserIdNotExist() {
        final long userId = validUser.getId();
        assertThrows(NotFoundException.class, () -> userService.getCommonFriends(3L, userId));
    }
}

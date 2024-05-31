package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.StorageData;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.friend.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {
    private static UserDbStorage userStorage;
    private static UserService userService;
    private static FriendshipDbStorage friendshipDbStorage;

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper = new UserRowMapper();

    private User validUser;
    private User friend;

    @BeforeEach
    void init() {
        userStorage = new UserDbStorage(jdbcTemplate, mapper);
        friendshipDbStorage = new FriendshipDbStorage(jdbcTemplate);
        userService = new UserService(userStorage, friendshipDbStorage);
        validUser = new User(
                1L,
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        // without id
        friend = new User(
                1L,
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
        User user = new User(
                123L,
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        // without id
        User user2 = new User(
                1234L,
                "test@test.ru",
                "LoginFriend",
                "Andy",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        userStorage.create(user);
        userStorage.create(user2);
        userService.addFriend(user.getId(), user2.getId());
        assertEquals(friendshipDbStorage.getFriendsIdByUserId(user.getId()), List.of(user2.getId()));
        assertNotEquals(friendshipDbStorage.getFriendsIdByUserId(user2.getId()), List.of(user.getId()));
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
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(99999L, userId));
    }

    @Test
    void successfulGetCommonFriend() {
        User user = new User(
                111L,
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );
        User user2 = new User(
                222L,
                "test@test.ru",
                "LoginFriend",
                "Andy",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );
        User user3 = new User(
                333L,
                "test@test.ru",
                "Garage",
                "Max",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

        userStorage.create(user);
        userStorage.create(user2);
        userStorage.create(user3);
        userService.addFriend(user.getId(), user2.getId());
        userService.addFriend(user3.getId(), user2.getId());

        assertEquals(1, userService.getCommonFriends(user.getId(), user3.getId()).size());
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

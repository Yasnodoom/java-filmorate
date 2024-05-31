package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.friend.FriendshipDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;
import static ru.yandex.practicum.filmorate.util.Utils.hasId;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Storage<User> userStorage;
    private final FriendshipDbStorage friendshipDbStorage;

    public void create(User user) {
        validate(user);
        replaceNameIfEmpty(user);
        user.setId(getNextId(userStorage.getAll()));
        userStorage.create(user);
    }

    public void update(User user) {
        if (!hasId(userStorage.getAll(), user.getId())) {
            throw new NotFoundException("не найден пользователь с id: " + user.getId());
        }

        validate(user);
        replaceNameIfEmpty(user);
        userStorage.update(user);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = validateUserId(userId);
        User friend = validateUserId(friendId);
        if (getFriends(userId).contains(friend)) {
            return;
        }

        user.getFriends().add(friendId);
        friendshipDbStorage.setFriendship(user);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validateUserId(friendId);
        User user = validateUserId(userId);

        user.getFriends().remove(friendId);
        friendshipDbStorage.deleteFriend(userId, friendId);
    }

    public Set<User> getFriends(Long userId) {
        validateUserId(userId);
        return friendshipDbStorage.getFriendsIdByUserId(userId)
                .stream()
                .map(userStorage::getElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        validateUserId(userId);
        validateUserId(otherId);

        return friendshipDbStorage.getFriendsIdByUserId(userId).stream()
                .distinct()
                .filter(friendshipDbStorage.getFriendsIdByUserId(otherId)::contains)
                .map(userStorage::getElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public User validateUserId(Long id) {
        return userStorage.getElement(id)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с id: " + id));
    }

    public void confirmFriend(long acceptedId, long alreadyFriendId) {
        addFriend(acceptedId,alreadyFriendId);
        friendshipDbStorage.updateFriendshipStatus(acceptedId,alreadyFriendId);
    }

    public void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank())
            throw new ValidationException("почта не может быть пустой");
        if (!user.getEmail().contains("@"))
            throw new ValidationException("почта должна содержать символ @");
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank())
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        if (user.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("дата рождения не может быть в будущем");
    }

    private void replaceNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

}

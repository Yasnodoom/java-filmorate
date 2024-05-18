package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;
import static ru.yandex.practicum.filmorate.util.Utils.hasId;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Storage<User> inMemoryUserStorage;

    public void create(User user) {
        validate(user);
        replaceNameIfEmpty(user);
        user.setId(getNextId(inMemoryUserStorage.getAll()));
        inMemoryUserStorage.create(user);
    }

    public void update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!hasId(inMemoryUserStorage.getAll(), user.getId())) {
            throw new NotFoundException("не найден пользователь с id: " + user.getId());
        }

        validate(user);
        replaceNameIfEmpty(user);
        inMemoryUserStorage.update(user);
    }

    public Collection<User> getAll() {
        return inMemoryUserStorage.getAll();
    }

    public void addFriend(Long userId, Long friendId) {
        User friend = validateUserId(friendId);
        User user = validateUserId(userId);
        if (user.getFriends().contains(friendId)) {
            return;
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User friend = validateUserId(friendId);
        User user = validateUserId(userId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<User> getFriends(Long userId) {
        return validateUserId(userId).getFriends().stream()
                .map(id -> inMemoryUserStorage.getElement(id).get())
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(Long userId, Long otherId) {
        User user = validateUserId(userId);
        User other = validateUserId(otherId);

        return user.getFriends().stream()
                .filter(n -> !other.getFriends().add(n))
                .map(id -> inMemoryUserStorage.getElement(id).get())
                .collect(Collectors.toSet());
    }

    public User validateUserId(Long id) {
        return inMemoryUserStorage.getElement(id)
                .orElseThrow(() -> new NotFoundException("не найден пользователь с id: " + id));
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

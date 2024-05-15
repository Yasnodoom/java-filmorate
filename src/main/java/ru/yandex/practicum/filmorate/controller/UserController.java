package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody final User user) {
        validate(user);
        replaceNameIfEmpty(user);
        user.setId(getNextId(users));
        users.put(user.getId(), user);
        log.info("User created {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody final User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("не найден пользователь с id: " + user.getId());
        }

        validate(user);
        replaceNameIfEmpty(user);

        users.put(user.getId(), user);
        log.info("User updated {}", user);
        return user;
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
        } else {
            user.setName(user.getName());
        }
    }
}

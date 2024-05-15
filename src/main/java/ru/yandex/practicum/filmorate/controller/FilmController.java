package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody final Film film) {
        validate(film);
        film.setId(getNextId(films));
        films.put(film.getId(), film);
        log.info("Film created {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody final Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("не найден пользователь с id: " + film.getId());
        }

        validate(film);
        films.put(film.getId(), film);
        log.info("Film updated {}", film);
        return film;
    }

    public void validate(final Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().isEmpty() || film.getDescription().isBlank()) {
            throw new ValidationException("описание не может быть пустым");
        }
        if (film.getDescription().length() > MAX_DESCRIPTION_SIZE) {
            throw new ValidationException("максимальная длина описания — " + MAX_DESCRIPTION_SIZE + " символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.from(MIN_RELEASE_DATE))) {
            throw new ValidationException("дата релиза — не раньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration().isNegative() || film.getDuration().isZero()) {
            throw new ValidationException("продолжительность фильма должна быть положительным числом");
        }
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;
import static ru.yandex.practicum.filmorate.util.Utils.hasId;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final Storage<Film> inMemoryFilmStorage;
    private final UserService userService;

    private static final int MAX_DESCRIPTION_SIZE = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public void create(Film film) {
        validate(film);
        film.setId(getNextId(inMemoryFilmStorage.getAll()));
        inMemoryFilmStorage.create(film);
    }

    public void update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("не указан id");
        }
        if (!hasId(inMemoryFilmStorage.getAll(), film.getId())) {
            throw new NotFoundException("не найден фильм с id: " + film);
        }

        validate(film);
        inMemoryFilmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return inMemoryFilmStorage.getAll();
    }

    public void addLike(long id, long userId) {
        Film film = validateFilmId(id);
        userService.validateUserId(userId);
        film.getLikes().add(userId);
    }

    public void deleteLike(long id, long userId) {
        Film film = validateFilmId(id);
        userService.validateUserId(userId);
        film.getLikes().remove(userId);

    }

    public List<Film> getPopular(int count) {
        return inMemoryFilmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> f.getLikes().size()))
                .limit(count)
                .toList().reversed();
    }


    public void validate(Film film) {
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

    private Film validateFilmId(Long id) {
        return inMemoryFilmStorage.getElement(id)
                .orElseThrow(() -> new NotFoundException("не найден фильм с id: " + id));
    }
}

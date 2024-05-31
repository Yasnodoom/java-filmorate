package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.util.Utils.getNextId;
import static ru.yandex.practicum.filmorate.util.Utils.hasId;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final Storage<Film> filmStorage;
    private final LikeDbStorage likeDbStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

    private static final int MAX_DESCRIPTION_SIZE = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public void create(Film film) {
        validate(film);
        film.setId(getNextId(filmStorage.getAll()));
        filmStorage.create(film);
    }

    public void update(Film film) {
        if (!hasId(filmStorage.getAll(), film.getId())) {
            throw new NotFoundException("не найден фильм с id: " + film);
        }

        validate(film);
        filmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(long id, long userId) {
        userService.validateUserId(userId);
        likeDbStorage.addLike(id, userId);
    }

    public void deleteLike(long id, long userId) {
        userService.validateUserId(userId);
        likeDbStorage.deleteLike(id, userId);
    }

    public List<Film> getPopular(int count) {
        return likeDbStorage.getPopular(count).stream()
                .map(filmStorage::getElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Film getFilm(long id) {
        Film film = filmStorage.getElement(id)
                .orElseThrow(() -> new NotFoundException("не найден фильм с id: " + id));
        film.setMpa(mpaService.getMpa(film.getMpa().getId()));
        film.setGenres(genreService.getGenreByFilmId(id));

        return film;
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
}

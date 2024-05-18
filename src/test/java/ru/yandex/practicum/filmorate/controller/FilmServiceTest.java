package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.Storage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmServiceTest {
    private static Storage<Film> filmStorage;
    private static Storage<User> userStorage;
    private static UserService userService;
    private static FilmService filmService;

    private Film validFilm;
    private User user;

    @BeforeEach
    void init() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, userService);
        validFilm = new Film(
                "Матрица",
                "Описание",
                LocalDate.of(1999, 12, 28),
                Duration.ofMinutes(120),
                new HashSet<>()
        );
        validFilm.setId(1L);
        user = new User(
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );
        user.setId(1L);

        filmStorage.create(validFilm);
        userStorage.create(user);
    }

    @Test
    void validateFilmOk() {
        filmService.validate(validFilm);
    }

    @Test
    void validateFilmFailEmptyName() {
        validFilm.setName("");
         assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmFailEmptyDescription() {
        validFilm.setDescription("");
        assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmFailIfDescriptionLengthEquals201() {
        validFilm.setDescription(String.valueOf('a').repeat(201));
        assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmOkIfDescriptionLengthEquals200() {
        validFilm.setDescription(String.valueOf('a').repeat(200));
        assertDoesNotThrow(() -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmFailIfReleaseDateLessMinDate() {
        validFilm.setReleaseDate(LocalDate.of(1799, 12, 28));
        assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmFailIfDurationIsNegative() {
        validFilm.setDuration(Duration.ofMinutes(-1));
        assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void validateFilmFailIfDurationIsZero() {
        validFilm.setDuration(Duration.ofMinutes(0));
        assertThrows(ValidationException.class, () -> filmService.validate(validFilm));
    }

    @Test
    void successfulAddLike() {
        assertEquals(0, validFilm.getLikes().size());
        filmService.addLike(validFilm.getId(), user.getId());
        assertEquals(1, validFilm.getLikes().size());
    }

    @Test
    void validateErrorWhenAddLikeAndFilmsIdNotExist() {
        final long userId = user.getId();
        assertThrows(NotFoundException.class, () ->  filmService.addLike(999L, userId));
    }

    @Test
    void validateErrorWhenAddLikeAndUserIdNotExist() {
        final long filmId = validFilm.getId();
        assertThrows(NotFoundException.class, () ->  filmService.addLike(filmId, 4L));
    }

    @Test
    void successfulDeleteLike() {
        successfulAddLike();
        assertEquals(1, validFilm.getLikes().size());
        filmService.deleteLike(validFilm.getId(), user.getId());
        assertEquals(0, validFilm.getLikes().size());
    }

    @Test
    void validateErrorWhenDeleteLikeAndFilmsIdNotExist() {
        final long userId = user.getId();
        assertThrows(NotFoundException.class, () ->  filmService.deleteLike(999L, userId));
    }

    @Test
    void getPopularFilmsIfFilmsEmptyShouldReturnZero() {
        FilmStorage emptyFilmStorage = new InMemoryFilmStorage();
        FilmService newFilmService = new FilmService(emptyFilmStorage, userService);
        final int films = newFilmService.getPopular(10).size();
        assertEquals(0, films);
    }

    @Test
    void getPopularFilmsIfFilmsExistShouldReturnFilms() {
        final int films = filmService.getPopular(10).size();
        assertEquals(1, films);
    }

    @Test
    void getPopularFilmsIfRequestZeroFilmsShouldReturnZero() {
        final int films = filmService.getPopular(0).size();
        assertEquals(0, films);
    }

}

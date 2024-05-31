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
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {
    private static FilmDbStorage filmStorage;
    private static UserDbStorage userStorage;
    private static UserService userService;
    private static FilmService filmService;
    private static FriendshipDbStorage friendshipDbStorage;
    private static LikeDbStorage likeDbStorage;

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper = new FilmRowMapper();

    private Film validFilm;
    private User user;

    @BeforeEach
    void init() {
        filmStorage = new FilmDbStorage(jdbcTemplate, mapper);

        userStorage = new UserDbStorage(jdbcTemplate, new UserRowMapper());
        friendshipDbStorage = new FriendshipDbStorage(jdbcTemplate);
        userService = new UserService(userStorage, friendshipDbStorage);
        filmService = new FilmService(
                filmStorage,
                new LikeDbStorage(jdbcTemplate),
                userService,
                new GenreService(new GenreDbStorage(jdbcTemplate, new GenreRowMapper())),
                new MpaService(new MpaDbStorage(jdbcTemplate, new MpaRowMapper()))
                );
        likeDbStorage =  new LikeDbStorage(jdbcTemplate);
        validFilm = new Film(
                1L,
                "Матрица",
                "Описание",
                LocalDate.of(1999, 12, 28),
                Duration.ofMinutes(120),
                new Mpa(1L),
                List.of(new Genre(1L, "ganre")),
                new HashSet<>()
        );
        user = new User(
                1L,
                "test@test.ru",
                "Login",
                "John",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );

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
        assertEquals(1, likeDbStorage.getPopular(1).size());
    }

    @Test
    void validateErrorWhenAddLikeAndFilmsIdNotExist() {
        final long userId = user.getId();
        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, userId));
    }

    @Test
    void validateErrorWhenAddLikeAndUserIdNotExist() {
        final long filmId = validFilm.getId();
        assertThrows(NotFoundException.class, () -> filmService.addLike(filmId, 4L));
    }

    @Test
    void successfulDeleteLike() {
        successfulAddLike();
        filmService.deleteLike(validFilm.getId(), user.getId());
        assertEquals(0, validFilm.getLikes().size());
    }

    @Test
    void validateErrorWhenDeleteLikeAndFilmsIdNotExist() {
        final long userId = user.getId();
        assertThrows(NotFoundException.class, () -> filmService.deleteLike(999L, userId));
    }

    @Test
    void getPopularFilmsIfFilmsEmptyShouldReturnZero() {
        FilmStorage emptyFilmStorage = new FilmDbStorage(jdbcTemplate, mapper);
        FilmService newFilmService = new FilmService(
                emptyFilmStorage,
                new LikeDbStorage(jdbcTemplate),
                new UserService(userStorage, new FriendshipDbStorage(jdbcTemplate)),
                new GenreService(new GenreDbStorage(jdbcTemplate, new GenreRowMapper())),
                new MpaService(new MpaDbStorage(jdbcTemplate, new MpaRowMapper()))
                );
        final int films = newFilmService.getPopular(10).size();
        assertEquals(0, films);
    }

    @Test
    void getPopularFilmsIfFilmsExistShouldReturnFilms() {
        successfulAddLike();
        final int films = filmService.getPopular(10).size();
        assertEquals(1, films);
    }

    @Test
    void getPopularFilmsIfRequestZeroFilmsShouldReturnZero() {
        final int films = filmService.getPopular(0).size();
        assertEquals(0, films);
    }

}

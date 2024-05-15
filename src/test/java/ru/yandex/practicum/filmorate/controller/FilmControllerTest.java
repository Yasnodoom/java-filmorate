package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private static FilmController filmController;

    @BeforeEach
    void init() {
        filmController = new FilmController();
    }

    @Test
    void validateFilmOk() {
        final Film validFilm = Film.builder()
                .id(1L)
                .name("Матрица")
                .description("Описание")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();

        filmController.validate(validFilm);
    }

    @Test
    void validateFilmFailEmptyName() {
        final Film film = Film.builder()
                .id(1L)
                .description("Описание")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmFailEmptyDescription() {
        final Film film = Film.builder()
                .id(1L)
                .name("Матрица")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmFailIfDescriptionLengthEquals201() {
        final Film film = Film.builder()
                .id(1L)
                .name("Матрица")
                .description(String.valueOf('a').repeat(201))
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmOkIfDescriptionLengthEquals200() {
        final Film film = Film.builder()
                .id(1L)
                .name("Матрица")
                .description(String.valueOf('a').repeat(201))
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();
        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmFailIfReleaseDateLessMinDate() {
        final Film film = Film.builder()
                .id(1L)
                .description("Описание")
                .releaseDate(LocalDate.of(1799, 12, 28))
                .duration(Duration.ofMinutes(120))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmFailIfDurationIsNegative() {
        final Film film = Film.builder()
                .id(1L)
                .description("Описание")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(-1))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

    @Test
    void validateFilmFailIfDurationIsZero() {
        final Film film = Film.builder()
                .id(1L)
                .description("Описание")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(Duration.ofMinutes(0))
                .build();

        assertThrows(ValidationException.class, () -> filmController.validate(film));
    }

}

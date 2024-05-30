package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getAll() {
        return genreDbStorage.getAll();
    }

    public Genre getGenre(long id) {
        return genreDbStorage.getGenre(id)
                .orElseThrow(() -> new NotFoundException("не найден жанр с id: " + id));
    }

    public List<Genre> getGenreByFilmId(String name, Duration duration, LocalDate date) {
        return genreDbStorage.getGenreByFilmId(name, duration, date);
    }
}

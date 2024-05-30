package ru.yandex.practicum.filmorate.storage.genre;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper mapper;

    public Collection<Genre> getAll() {
        return jdbcTemplate.query("SELECT * FROM genre", mapper);
    }

    public Optional<Genre> getGenre(Long id) {
        String query = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            Genre result = jdbcTemplate.queryForObject(query, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenreByFilmId(String name, Duration duration, LocalDate date) {
        return jdbcTemplate.query("""
                        SELECT g.genre_id, g.name
                        FROM GENRE g
                        WHERE g.GENRE_ID IN (SELECT DISTINCT f.genre_id
                        FROM FILMS f
                        WHERE f.NAME = ? AND f.DURATION = ? AND f.RELEASE_DATE = ?)
                        """,
                mapper, name, duration, date);
    }
}

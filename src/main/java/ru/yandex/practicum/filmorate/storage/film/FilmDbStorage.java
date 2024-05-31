package ru.yandex.practicum.filmorate.storage.film;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Component
@Primary
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public void create(Film data) {
        String sqlQuery = """
                INSERT INTO films (name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, data.getName());
            stmt.setString(2, data.getDescription());
            stmt.setDate(3, Date.valueOf(data.getReleaseDate()));
            stmt.setLong(4, data.getDuration().getSeconds());
            stmt.setLong(5, data.getMpa().getId());
            return stmt;
        }, keyHolder);

        data.setId(keyHolder.getKey().longValue());
        addLinkGenreFilmsToDb(data);
    }

    @Override
    public void update(Film data) {
        String sqlQuery = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?,
                    duration = ?, rating_id = ?
                WHERE film_id = ?
                """;

        jdbcTemplate.update(
                sqlQuery,
                data.getName(),
                data.getDescription(),
                data.getReleaseDate(),
                data.getDurationTimeSeconds(),
                data.getMpa().getId(),
                data.getId()
        );
    }

    @Override
    public Collection<Film> getAll() {
        return jdbcTemplate.query("SELECT film_id, name, description, release_date, duration, rating_id FROM films", mapper);
    }

    @Override
    public Optional<Film> getElement(Long id) {
        try {
            Film result = jdbcTemplate.queryForObject(
                    "SELECT film_id, name, description, release_date, duration, rating_id FROM films WHERE film_id = ?",
                    mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private void addLinkGenreFilmsToDb(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        String sqlQuery = "INSERT INTO genres_films (film_id, genre_id) VALUES (?, ?)";
        Set<Genre> genres = Set.copyOf(film.getGenres());

        for (Genre g : genres) {
            jdbcTemplate.update(sqlQuery, film.getId(), g.getId());
        }
    }
}

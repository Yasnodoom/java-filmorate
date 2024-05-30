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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.Optional;

@Component
@Primary
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    private static final String FIND_FILM_BY_ID = """
            SELECT f.film_id id, f.name name, f.description desc, f.release_date rd, f.duration d,
            g.genre_id gi, g.name gn, r.rating_id ri, r.name rn
            FROM films f
            LEFT JOIN genre g ON f.genre_id = g.genre_id
            LEFT JOIN rating r ON f.rating_id = r.rating_id
            WHERE film_id = ?
            """;

    private static final String FIND_ALL_FILMS = """
            SELECT f.film_id id, f.name name, f.description desc, f.release_date rd, f.duration d,
            g.genre_id gi, g.name gn, r.rating_id ri, r.name rn
            FROM films f
            LEFT JOIN genre g ON f.genre_id = g.genre_id
            LEFT JOIN rating r ON f.rating_id = r.rating_id
            """;

    @Override
    public void create(Film data) {
        String sqlQuery = """
                INSERT INTO films (name, description, release_date, duration, genre_id, rating_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        int genres;
        if (data.getGenres() != null) {
            genres = data.getGenres().size();
        } else {
            genres = 1;
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();

        for (int i = 0; i < genres; i++) {
            int finalI = i;
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
                stmt.setString(1, data.getName());
                stmt.setString(2, data.getDescription());
                stmt.setDate(3, Date.valueOf(data.getReleaseDate()));
                stmt.setLong(4, data.getDuration().getSeconds());
                if (data.getGenres() == null) {
                    stmt.setNull(5, Types.BIGINT);
                } else {
                    stmt.setObject(5, data.getGenres().get(finalI).getId(), Types.BIGINT);
                }
                stmt.setLong(6, data.getMpa().getId());
                return stmt;
            }, keyHolder);
            data.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void update(Film data) {
        String sqlQuery = "update films SET name = ?, description = ?, release_date = ?," +
                " duration = ?, genre_id = ?, rating_id = ? WHERE film_id = ?";

        if (data.getGenres() != null) {
            for (int i = 0; i < data.getGenres().size(); i++) {
                jdbcTemplate.update(
                        sqlQuery,
                        data.getName(),
                        data.getDescription(),
                        data.getReleaseDate(),
                        data.getDurationTimeSeconds(),
                        data.getGenres().get(i).getId(),
                        data.getMpa().getId(),
                        data.getId()
                );
            }
        }
    }

    @Override
    public Collection<Film> getAll() {
        return jdbcTemplate.query(FIND_ALL_FILMS, mapper);
    }

    @Override
    public Optional<Film> getElement(Long id) {
        try {
            Film result = jdbcTemplate.queryForObject(FIND_FILM_BY_ID, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

}

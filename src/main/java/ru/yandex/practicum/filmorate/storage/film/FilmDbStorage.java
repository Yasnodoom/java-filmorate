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

    @Override
    public void create(Film data) {
        String sqlQuery = """
                insert into films (
                    name, description, release_date, duration, genre_id, rating_id
                )
                values (?, ?, ?, ?, ?, ?)
                """;

        Long genreId = null;
        if (data.getGenres() != null) {
            genreId = data.getGenres().getFirst().getId();
        }
        final Long finalGenreId = genreId;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, data.getName());
            stmt.setString(2, data.getDescription());
            stmt.setDate(3, Date.valueOf(data.getReleaseDate()));
            stmt.setLong(4, data.getDuration().getSeconds());
            stmt.setObject(5, finalGenreId, Types.BIGINT);
            stmt.setLong(6, data.getMpa().getId());
            return stmt;
        }, keyHolder);

        data.setId(keyHolder.getKey().longValue());
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
        return jdbcTemplate.query("select * from films", mapper);
    }

    @Override
    public Optional<Film> getElement(Long id) {
        String query = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film result = jdbcTemplate.queryForObject(query, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

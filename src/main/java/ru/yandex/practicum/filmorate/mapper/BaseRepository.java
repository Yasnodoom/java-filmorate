package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;


    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
    }

    protected boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected Collection<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

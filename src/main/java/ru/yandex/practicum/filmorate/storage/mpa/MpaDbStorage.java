package ru.yandex.practicum.filmorate.storage.mpa;


import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mapper;

    public Collection<Mpa> getAll() {
        return jdbcTemplate.query("SELECT * FROM rating", mapper);
    }

    public Optional<Mpa> getMpa(Long id) {
        String query = "SELECT * FROM rating WHERE rating_id = ?";
        try {
            Mpa result = jdbcTemplate.queryForObject(query, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

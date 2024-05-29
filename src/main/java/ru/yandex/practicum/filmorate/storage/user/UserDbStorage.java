package ru.yandex.practicum.filmorate.storage.user;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Component
@Primary
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;

    @Override
    public void create(User data) {
        String sqlQuery = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sqlQuery,
                data.getEmail(),
                data.getLogin(),
                data.getName(),
                data.getBirthday()
        );
    }

    @Override
    public void update(User data) {
        String sqlQuery = """
                UPDATE users SET
                email = ?, login = ?, name = ?, birthday = ?
                WHERE user_id = ?
                """;

        jdbcTemplate.update(
                sqlQuery,
                data.getEmail(),
                data.getLogin(),
                data.getName(),
                data.getBirthday(),
                data.getId()
        );
    }

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM users", mapper);
    }

    @Override
    public Optional<User> getElement(Long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try {
            User result = jdbcTemplate.queryForObject(query, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

package ru.yandex.practicum.filmorate.storage.like;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public void addLike(Long filmId, Long userId) {
        String sqlQuery = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    public List<Long> getPopular(int count) {
        String sqlQuery = """
                SELECT film_id
                FROM ( SELECT film_id, count(*) likes FROM LIKES l
                       GROUP BY FILM_ID
                       ORDER BY likes DESC
                       LIMIT ?
                     )
                """;
        return jdbcTemplate.queryForList(sqlQuery, Long.class, count);
    }
}

package ru.yandex.practicum.filmorate.storage.friend;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
@AllArgsConstructor
public class FriendshipDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Long> getFriendsIdByUserId(Long id) {
        return jdbcTemplate.queryForList("SELECT friend_id FROM friendship WHERE user_id = ?", Long.class, id);
    }

    public void setFriendship(User user) {
        final Long userId = user.getId();
        String sqlQuery = "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, ?)";

        for (Long friendId : user.getFriends()) {
            jdbcTemplate.update(sqlQuery, userId, friendId, 0);
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update("DELETE FROM friendship WHERE user_id = ? AND friend_id = ?;", userId, friendId);
    }

    public void updateFriendshipStatus(Long userId, Long friendId) {
        String sqlQuery = "UPDATE friendship SET status_id = 1 WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sqlQuery, userId, friendId);
        jdbcTemplate.update(sqlQuery, friendId, userId);
    }
}
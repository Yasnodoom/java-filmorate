package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa rating = new Mpa();
        rating.setId(rs.getLong("rating_id"));
        rating.setName(rs.getString("name"));
        rating.setDesc(rs.getString("description"));
        return rating;
    }
}

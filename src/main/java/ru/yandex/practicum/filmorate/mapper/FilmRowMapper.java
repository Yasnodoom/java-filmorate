package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(Duration.ofSeconds(rs.getInt("duration")));
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("rating_id" ));
        film.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(rs.getLong("genre_id"));
        List<Genre> genres = new ArrayList<>();
        genres.add(genre);
        film.setGenres(genres);

        return film;
    }
}

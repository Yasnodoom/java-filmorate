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

import static ru.yandex.practicum.filmorate.util.Utils.convertToLocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("desc"));
        film.setDuration(Duration.ofSeconds(rs.getInt("d")));
        film.setReleaseDate(convertToLocalDate(rs.getString("rd")));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("ri" ));
        mpa.setName(rs.getString("rn"));
        film.setMpa(mpa);

        if (rs.getLong("gi") != 0) {
            Genre genre = new Genre();
            genre.setId(rs.getLong("gi"));
            genre.setName(rs.getString("gn"));
            List<Genre> genres = new ArrayList<>();
            genres.add(genre);
            film.setGenres(genres);
        }


        return film;
    }
}

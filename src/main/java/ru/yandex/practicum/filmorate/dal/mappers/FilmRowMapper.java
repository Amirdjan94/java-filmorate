package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film.film_id"));
        film.setName(rs.getString("film.name"));
        film.setDuration(rs.getInt("film.duration"));
        film.setDescription(rs.getString("film.description"));
        film.setReleaseDate(rs.getDate("film.releaseDate").toLocalDate());
        int mpaId = rs.getInt("film.ratingMpaId");
        String mpaName = rs.getString("rating_mpa.ratingMPAname");
        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId((long) mpaId);
            mpa.setName(mpaName);
            film.setMpa(mpa);
        }
//        int [] genresIds = rs.getArray("")
////        = rs.getString()
        return film;
    }



//    // Получаем имя рейтинга (mpa_name - алиас из запроса)
//    String mpaName = rs.getString("mpa_name");
//        if (mpaName != null) {
//        mpa.setName(mpaName);
//    }
//
//        film.setMpa(mpa);
//
//    private static final String FIND_FILM_BY_ID = "SELECT * FROM film " +
//            "JOIN rating_mpa ON film.ratingMpaId = rating_mpa.ratingMpaId WHERE film.film_id = ?";
}

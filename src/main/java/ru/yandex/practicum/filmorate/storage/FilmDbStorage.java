package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS = "SELECT * FROM film";
    private static final String FIND_FILM_BY_ID = "SELECT * FROM film WHERE film_id = ?";
    private static final String INSERT_FILM = "INSERT INTO film(name, description, releaseDate, duration)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE film SET name = ?, description = ?, releaseDate = ?, duration = ? WHERE film_id = ?";
    private static final String GET_LIKES_COUNT = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
    private static final String INSERT_LIKE = "INSERT INTO film_likes(film_id, user_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film newFilm, Film currentFilm) {
        update(
                UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                currentFilm.getId()
        );
        return getFilmById(currentFilm.getId()).get();
    }

    @Override
    public List<Film> getFilms() {
        return findMany(FIND_ALL_FILMS);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return findOne(FIND_FILM_BY_ID, filmId);
    }

    @Override
    public Collection<Film> getMostPopularFilms(int count) {
        List<Film> films = findMany(FIND_ALL_FILMS);
        Map<Film, Integer> map = films.stream()
                .collect(Collectors.toMap(film -> film, film -> getLikesCount(film))); //TODO: Перенести в baserepos
        return map.entrySet().stream()
                .sorted(Map.Entry.<Film, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(Film film, User user) {
        jdbc.update(//TODO: Перенести в baserepos
                INSERT_LIKE,
                film.getId(),
                user.getId()
        );
    }

    @Override
    public boolean deleteLike(Film film, User user) {
        return delete(DELETE_LIKE, film.getId(), user.getId());
    }

    private Integer getLikesCount(Film film) {
        return jdbc.queryForObject(GET_LIKES_COUNT, Integer.class, film.getId());
    }

    public void deleteAllFilms() {
        jdbc.execute("DELETE FROM film");
    }
}

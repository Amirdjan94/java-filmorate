package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS = "SELECT * FROM film " +
            "JOIN rating_mpa ON film.ratingMpaId = rating_mpa.ratingMpaId";
    private static final String FIND_MOST_POPULAR_FILMS =
            "SELECT film.*, rating_mpa.ratingMPAname FROM film_likes " +
                    "JOIN film ON film.film_id = film_likes.film_id " +
                    "JOIN rating_mpa ON film.ratingMpaId = rating_mpa.ratingMpaId " +
                    "GROUP BY film.film_id, film.name, film.description, film.releaseDate, " +
                    "film.duration, film.ratingMpaId, rating_mpa.ratingMPAname " +
                    "ORDER BY COUNT(film_likes.film_id) DESC " +
                    "LIMIT ?";
    private static final String FIND_FILM_BY_ID = "SELECT * FROM film " +
            "JOIN rating_mpa ON film.ratingMpaId = rating_mpa.ratingMpaId " +
            "WHERE film.film_id = ?";
    private static final String FIND_GENRES_BY_FILM_ID = "SELECT genres.* FROM genres " +
            "JOIN film_genres ON film_genres.genre_id = genres.genre_id " +
            "WHERE film_genres.film_id = ?";
    private static final String INSERT_FILM = "INSERT INTO film(name, description, releaseDate, duration, ratingMpaId)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_GENRES_WITH_FILM = "INSERT INTO film_genres(film_id, genre_id)" +
            "VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE film SET name = ?, description = ?, releaseDate = ?, " +
            "duration = ?, ratingMpaId = ? WHERE film_id = ?";
    private static final String GET_LIKES_COUNT = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
    private static final String INSERT_LIKE = "INSERT INTO film_likes(film_id, user_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_FILMS_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

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
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        insertGenresWithFilm(film);
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
                newFilm.getMpa().getId(),
                currentFilm.getId()
        );
        if (currentFilm.getGenres().size() != 0) {
            delete(DELETE_FILMS_GENRES, currentFilm.getId());
            insertGenresWithFilm(newFilm);
        }

        return getFilmById(currentFilm.getId()).get();
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = findMany(FIND_ALL_FILMS);
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Set<Genres>> genresMap = new HashMap<>();
        jdbc.query(
                "SELECT fg.film_id, g.genre_id, g.genre_name " +
                        "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (" +
                        films.stream().map(Film::getId).map(Object::toString)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );
        films.forEach(f -> f.setGenres(genresMap.getOrDefault(f.getId(), new HashSet<>())));
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        Optional<Film> film = findOne(FIND_FILM_BY_ID, filmId);
        if (film.isPresent()) {
            Collection<Genres> genresCollection = jdbc.query(
                    FIND_GENRES_BY_FILM_ID,
                    (rs, rowNum) -> new Genres(
                            rs.getLong("genre_id"),
                            rs.getString("genre_name")
                    ),
                    filmId
            );
            film.get().setGenres(new HashSet<>(genresCollection));
        }
        return film;
    }

    @Override
    public Collection<Film> getMostPopularFilms(int count) {
        List<Film> films = findMany(FIND_MOST_POPULAR_FILMS, count);
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Set<Genres>> genresMap = new HashMap<>();
        jdbc.query(
                "SELECT fg.film_id, g.genre_id, g.genre_name " +
                        "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (" +
                        films.stream().map(Film::getId).map(Object::toString)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );
        films.forEach(f -> f.setGenres(genresMap.getOrDefault(f.getId(), new HashSet<>())));
        return films;
    }

    @Override
    public void addLike(Film film, User user) {
        jdbc.update(
                INSERT_LIKE,
                film.getId(),
                user.getId()
        );
    }

    @Override
    public boolean deleteLike(Film film, User user) {
        return delete(DELETE_LIKE, film.getId(), user.getId());
    }

    public Collection<Film> getCommonUserFilms(Long userId, Long friendId) {
        String sql = "SELECT f.film_id, f.name, f.description, f.releaseDate, f.duration, " +
                "f.ratingMpaId, rm.ratingMPAname " +
                "FROM film f " +
                "JOIN film_likes fl1 ON f.film_id = fl1.film_id " +
                "JOIN film_likes fl2 ON f.film_id = fl2.film_id " +
                "LEFT JOIN rating_mpa rm ON f.ratingMpaId = rm.ratingMpaId " +
                "WHERE fl1.user_id = ? AND fl2.user_id = ? " +
                "GROUP BY f.film_id, f.name, f.description, f.releaseDate, f.duration, f.ratingMpaId, rm.ratingMPAname " +
                "ORDER BY COUNT(*) DESC";

        List<Film> films = jdbc.query(sql, new FilmRowMapper(), userId, friendId);

        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Set<Genres>> genresMap = new HashMap<>();
        jdbc.query(
                "SELECT fg.film_id, g.genre_id, g.genre_name " +
                        "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (" +
                        films.stream().map(Film::getId).map(Object::toString)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );

        films.forEach(f -> f.setGenres(genresMap.getOrDefault(f.getId(), new HashSet<>())));
        return films;
    }

    private Integer getLikesCount(Film film) {
        return jdbc.queryForObject(GET_LIKES_COUNT, Integer.class, film.getId());
    }

    public void deleteAllFilms() {
        jdbc.execute("DELETE FROM film");
    }

    private void insertGenresWithFilm(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (Genres genre : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
            jdbc.batchUpdate(INSERT_GENRES_WITH_FILM, batchArgs);
        }

    }
}

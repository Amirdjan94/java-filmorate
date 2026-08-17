package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Director;
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
    private static final String INSERT_DIRECTORS_WITH_FILM = "INSERT INTO film_directors(film_id, director_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_FILMS_DIRECTORS = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_BY_DIRECTOR_SORT_BY_LIKES =
            "SELECT f.*, r.ratingMPAname, " +
                    "(SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.film_id) AS likes_count " +
                    "FROM film f " +
                    "JOIN rating_mpa r ON f.ratingMpaId = r.ratingMpaId " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY likes_count DESC";
    private static final String FIND_BY_DIRECTOR_SORT_BY_YEAR =
            "SELECT f.*, r.ratingMPAname FROM film f " +
                    "JOIN rating_mpa r ON f.ratingMpaId = r.ratingMpaId " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.releaseDate";
    private static final String FIND_DIRECTORS_BY_FILM_ID = "SELECT d.director_id, d.name, d.lastname FROM directors d " +
            "JOIN film_directors fd ON d.director_id = fd.director_id " +
            "WHERE fd.film_id = ?";
    private static final String DELETE_FILM = "DELETE FROM film WHERE film_id = ?";

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
        insertDirectorsWithFilm(film);
        return getFilmById(id).get();
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

        delete(DELETE_FILMS_DIRECTORS, currentFilm.getId());
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            insertDirectorsWithFilm(newFilm);
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
                                .collect(Collectors.joining(",")) + ") " +
                        "ORDER BY g.genre_id",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );
        Map<Long, Set<Director>> directorMap = new HashMap<>();
        jdbc.query(
                "SELECT fd.film_id, d.director_id, d.name, d.lastname " +
                        "FROM film_directors fd JOIN directors d ON fd.director_id = d.director_id " +
                        "WHERE fd.film_id IN (" +
                        films.stream().map(Film::getId).map(String::valueOf)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    directorMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Director(
                                    rs.getLong("director_id"),
                                    rs.getString("name"),
                                    rs.getString("lastname")
                            ));
                }
        );

        films.forEach(f -> {
            f.setGenres(genresMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
            f.setDirectors(directorMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
        });
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
            film.get().setGenres(new LinkedHashSet<>(genresCollection));

            Collection<Director> directors = jdbc.query(
                    FIND_DIRECTORS_BY_FILM_ID,
                    (rs, rowNum) -> new Director(
                            rs.getLong("director_id"),
                            rs.getString("name"),
                            rs.getString("lastname")
                    ),
                    filmId
            );
            film.get().setDirectors(new LinkedHashSet<>(directors));
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
                                .collect(Collectors.joining(",")) + ") " +
                        "ORDER BY g.genre_id",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );
        Map<Long, Set<Director>> directorMap = new HashMap<>();
        jdbc.query(
                "SELECT fd.film_id, d.director_id, d.name, d.lastname " +
                        "FROM film_directors fd JOIN directors d ON fd.director_id = d.director_id " +
                        "WHERE fd.film_id IN (" +
                        films.stream().map(Film::getId).map(String::valueOf)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    directorMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Director(
                                    rs.getLong("director_id"),
                                    rs.getString("name"),
                                    rs.getString("lastname")
                            ));
                }
        );

        films.forEach(f -> {
            f.setGenres(genresMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
            f.setDirectors(directorMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
        });
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

    @Override
    public List<Film> getPopular(int count,
                                 Integer genreId,
                                 Integer year) {

        StringBuilder sql = new StringBuilder("""
                SELECT film.*, rating_mpa.ratingMPAname
                FROM film
                LEFT JOIN film_likes
                    ON film.film_id = film_likes.film_id
                JOIN rating_mpa
                    ON film.ratingMpaId = rating_mpa.ratingMpaId
                """);

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("""
                    JOIN film_genres
                        ON film.film_id = film_genres.film_id
                    """);
        }

        sql.append(" WHERE 1=1 ");

        if (genreId != null) {
            sql.append(" AND film_genres.genre_id = ? ");
            params.add(genreId);
        }

        if (year != null) {
            sql.append(" AND YEAR(film.releaseDate) = ? ");
            params.add(year);
        }

        sql.append("""
                GROUP BY film.film_id,
                         film.name,
                         film.description,
                         film.releaseDate,
                         film.duration,
                         film.ratingMpaId,
                         rating_mpa.ratingMPAname
                ORDER BY COUNT(film_likes.user_id) DESC
                LIMIT ?
                """);

        params.add(count);

        return findMany(
                sql.toString(),
                params.toArray());
    }

    @Override
    public void deleteFilm(long filmId) {
        jdbc.update(
                DELETE_FILM,
                filmId
        );
    }
    private void insertDirectorsWithFilm(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Object[]> batchArgs = new ArrayList<>();
            for (Director d : film.getDirectors()) {
                batchArgs.add(new Object[]{film.getId(), d.getId()});
            }
            jdbc.batchUpdate(INSERT_DIRECTORS_WITH_FILM, batchArgs);
        }
    }

    @Override
    public Collection<Film> getByDirector(Long id, String sortBy) {
        if (!"year".equalsIgnoreCase(sortBy) && !"likes".equalsIgnoreCase(sortBy)) {
            throw new ConditionsNotMetException("Параметр sortBy должен быть 'year' или 'likes'");
        }
        String query = sortBy.equalsIgnoreCase("year") ?
                FIND_BY_DIRECTOR_SORT_BY_YEAR : FIND_BY_DIRECTOR_SORT_BY_LIKES;
        List<Film> films = findMany(query, id);
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Set<Genres>> genresMap = new HashMap<>();
        jdbc.query(
                "SELECT fg.film_id, g.genre_id, g.genre_name " +
                        "FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (" +
                        films.stream().map(Film::getId).map(Object::toString)
                                .collect(Collectors.joining(",")) + ") " +
                        "ORDER BY g.genre_id",
                rs -> {
                    genresMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Genres(rs.getLong("genre_id"), rs.getString("genre_name")));
                }
        );
        Map<Long, Set<Director>> directorMap = new HashMap<>();
        jdbc.query(
                "SELECT fd.film_id, d.director_id, d.name, d.lastname " +
                        "FROM film_directors fd JOIN directors d ON fd.director_id = d.director_id " +
                        "WHERE fd.film_id IN (" +
                        films.stream().map(Film::getId).map(Object::toString)
                                .collect(Collectors.joining(",")) + ")",
                rs -> {
                    directorMap.computeIfAbsent(rs.getLong("film_id"), k -> new LinkedHashSet<>())
                            .add(new Director(rs.getLong("director_id"), rs.getString("name"),
                                    rs.getString("lastname")));
                }
        );
        films.forEach(f -> {
            f.setGenres(genresMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
            f.setDirectors(directorMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
        });

        return films;
    }
}

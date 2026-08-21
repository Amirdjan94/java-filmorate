package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
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
    private static final String FIND_COUNT_ALL_FILMS = "SELECT COUNT(*) FROM film";

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

        }
        insertGenresWithFilm(newFilm);
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
        loadGenres(films);
        loadDirectors(films);
        loadLikes(films);
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
        loadGenres(films);
        loadDirectors(films);
        loadLikes(films);
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

    @Override
    public Collection<Film> getUserRecommendations(User targetUser) {
        log.info("Поиск рекомендаций для пользователя с ID-" + targetUser.getId() + " на основе пересечений лайков");

        Integer filmsCount = jdbc.queryForObject(FIND_COUNT_ALL_FILMS, Integer.class);
        if (filmsCount == null || filmsCount == 0) {
            throw new ObjectNotFoundException("Фильмы не найдены");
        }

        // проверяем наличие лайков у пользователя
        Integer targetUserLikesCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE user_id = ?",
                Integer.class,
                targetUser.getId()
        );

        if (targetUserLikesCount == null || targetUserLikesCount == 0) {
            log.info("У целевого пользователя нет лайков, рекомендации невозможны");
            return Collections.emptyList();
        }

        // получаем список пользователей, которые лайкали те же фильмы что и таргет
        String sql = "SELECT DISTINCT fl2.user_id " +
                "FROM film_likes fl1 " +
                "JOIN film_likes fl2 ON fl1.film_id = fl2.film_id " +
                "WHERE fl1.user_id = ? AND fl2.user_id != ? " +
                "ORDER BY fl2.user_id DESC";
        List<Long> sameUsersLikes = jdbc.queryForList(sql, Long.class, targetUser.getId(), targetUser.getId());

        if (sameUsersLikes.isEmpty()) {
            log.info("Не найдено пользователей с общими лайками");
            return Collections.emptyList();
        }

        log.info("Найдено {} похожих пользователей", sameUsersLikes.size());

        // формируем плейсхолдеры для IN
        String placeholders = String.join(",", Collections.nCopies(sameUsersLikes.size(), "?"));

        String recommendSql = "SELECT " +
                "    fl.film_id AS \"film.film_id\", " +
                "    f.name AS \"film.name\", " +
                "    f.duration AS \"film.duration\", " +
                "    f.description AS \"film.description\", " +
                "    f.releaseDate AS \"film.releaseDate\", " +
                "    f.ratingMpaId AS \"film.ratingMpaId\", " +
                "    rmp.ratingMPAname AS \"rating_mpa.ratingMPAname\", " +
                "    COUNT(*) AS likes_count " +
                "FROM " +
                "    film_likes fl " +
                "JOIN " +
                "    film f ON fl.film_id = f.film_id " +
                "LEFT JOIN " +
                "    rating_mpa rmp ON f.ratingMpaId = rmp.ratingMpaId " +
                "WHERE " +
                "    fl.user_id IN (" + placeholders + ") " +
                "    AND fl.film_id NOT IN ( " +
                "        SELECT film_id " +
                "        FROM film_likes " +
                "        WHERE user_id = ? " +
                "    ) " +
                "GROUP BY " +
                "    fl.film_id, f.name, f.duration, f.description, f.releaseDate, " +
                "    f.ratingMpaId, rmp.ratingMPAname " +
                "ORDER BY " +
                "    likes_count DESC, " +
                "    fl.film_id";

        // Формируем массив параметров
        Object[] params = new Object[sameUsersLikes.size() + 1];
        for (int i = 0; i < sameUsersLikes.size(); i++) {
            params[i] = sameUsersLikes.get(i);
        }
        params[sameUsersLikes.size()] = targetUser.getId();

        // выполняем запрос и возвращаем список фильмов
        List<Film> recommendations = jdbc.query(recommendSql, params, mapper);
        loadGenres(recommendations);

        log.info("Найдено рекомендаций: {}", recommendations.size());
        return recommendations;
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

        List<Film> films = findMany(sql, userId, friendId);

        if (films.isEmpty()) {
            return films;
        }

        loadGenres(films);

        return films;
    }

    private Integer getLikesCount(Film film) {
        return jdbc.queryForObject(GET_LIKES_COUNT, Integer.class, film.getId());
    }

    public void deleteAllFilms() {
        jdbc.execute("DELETE FROM film");
    }

    private void insertGenresWithFilm(Film film) {
        log.info("insertGenresWithFilm: filmId={}, genres={}", film.getId(), film.getGenres());
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

        List<Film> films = findMany(sql.toString(), params.toArray());
        if (!films.isEmpty()) {
            loadGenres(films);
            loadDirectors(films);
            loadLikes(films);
        }
        return films;
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
        loadGenres(films);
        loadDirectors(films);
        loadLikes(films);

        return films;
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        StringBuilder sql = new StringBuilder("SELECT f.*, r.ratingMPAname FROM film f " +
                "LEFT JOIN rating_mpa r ON f.ratingMpaId = r.ratingMpaId ");
        if (by.toLowerCase().contains("director")) {
            sql.append("LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id ");
        }
        sql.append(" WHERE 1=1 ");

        String byLower = by.toLowerCase();

        if (byLower.contains("title") && byLower.contains("director")) {
            sql.append("AND (LOWER(f.name) LIKE LOWER(?) OR " +
                    "LOWER(d.name) LIKE LOWER(?) OR " +
                    "LOWER(d.lastname) LIKE LOWER(?)) ");
        } else if (byLower.contains("title")) {
            sql.append("AND LOWER(f.name) LIKE LOWER(?) ");
        } else if (byLower.contains("director")) {
            sql.append("AND (LOWER(d.name) LIKE LOWER(?) OR LOWER(d.lastname) LIKE LOWER(?)) ");
        }

        List<Object> params = new ArrayList<>();
        String searchParams = "%" + query.toLowerCase() + "%";
        if (by.toLowerCase().contains("title")) {
            params.add(searchParams);
        }
        if (by.toLowerCase().contains("director")) {
            params.add(searchParams);
            params.add(searchParams);
        }

        List<Film> films = findMany(sql.toString(), params.toArray());

        if (!films.isEmpty()) {
            loadGenres(films);
            loadDirectors(films);
            loadLikes(films);
        }
        films.sort((f1, f2) -> {
            int likesCompare = Integer.compare(
                    f2.getLikes() == null ? 0 : f2.getLikes().size(),
                    f1.getLikes() == null ? 0 : f1.getLikes().size()
            );
            if (likesCompare != 0) {
                return likesCompare;
            }
            return Long.compare(f1.getId(), f2.getId());
        });
        log.info("searchFilms: query={}, by={}, sql={}, params={}", query, by, sql, params);
        return films;

    }

    private void loadGenres(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }
        log.info("loadGenres called for {} films", films.size());
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
        films.forEach(f -> {
            f.setGenres(genresMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
        });
    }

    private void loadDirectors(List<Film> films) {
        log.info("loadDirectors called with films: {}", films);
        if (films == null || films.isEmpty()) {
            return;
        }
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
            f.setDirectors(directorMap.getOrDefault(f.getId(), new LinkedHashSet<>()));
        });
    }

    private void loadLikes(List<Film> films) {
        String ids = films.stream()
                .map(Film::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Map<Long, Set<Long>> likesMap = new HashMap<>();
        jdbc.query(
                "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + ids + ")",
                rs -> {
                    likesMap.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>())
                            .add(rs.getLong("user_id"));
                }
        );

        films.forEach(f -> f.setLikes(likesMap.getOrDefault(f.getId(), new HashSet<>())));
    }
}
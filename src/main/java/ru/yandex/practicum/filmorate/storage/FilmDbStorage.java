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
    public Collection<Film> getUserRecommendations(User targetUser, Collection<User> allUsers) {
        log.info("Поиск рекомендаций для пользователя с ID-" + targetUser.getId() + " на основе пересечений лайков");

        if (getFilms().isEmpty()) {
            throw new ObjectNotFoundException("Фильмы не найдены");
        }

        // получаем все лайки из БД одним запросом
        Map<Long, Set<Long>> userLikesMap = loadAllUserLikes();

        // получаем лайки целевого пользователя
        Set<Long> targetUserLikes = userLikesMap.getOrDefault(targetUser.getId(), new HashSet<>());

        if (targetUserLikes.isEmpty()) {
            log.info("У целевого пользователя нет лайков, рекомендации невозможны");
            return Collections.emptyList();
        }

        // находим пользователей с максимальным пересечением по лайкам
        List<UserSimilarity> similarities = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getId().equals(targetUser.getId())) {
                continue; // пропускаем самого себя
            }

            Set<Long> userLikes = userLikesMap.getOrDefault(user.getId(), new HashSet<>());

            // вычисляем пересечение (количество общих лайков)
            Set<Long> intersection = new HashSet<>(targetUserLikes);
            intersection.retainAll(userLikes);

            int commonCount = intersection.size();

            // если есть хотя бы 1 общий лайк - добавляем в список
            if (commonCount > 0) {
                // Вычисляем фильмы, которые лайкнул этот пользователь, но не лайкнул целевой
                Set<Long> newFilms = new HashSet<>(userLikes);
                newFilms.removeAll(targetUserLikes);

                similarities.add(new UserSimilarity(user, commonCount, newFilms));
            }
        }

        // сортируем по количеству общих лайков (по убыванию)
        similarities.sort((a, b) -> Integer.compare(b.commonCount, a.commonCount));

        if (similarities.isEmpty()) {
            log.info("Не найдено пользователей с общими лайками");
            return Collections.emptyList();
        }

        // выбираем топ-5 похожих пользователей (или всех, если их меньше)
        int topN = Math.min(5, similarities.size());
        List<UserSimilarity> topUsers = similarities.subList(0, topN);

        log.info("Найдено {} похожих пользователей, берем топ-{}", similarities.size(), topN);

        // собираем рекомендуемые фильмы с весом (количество похожих пользователей, которые его лайкнули)
        Map<Long, Integer> filmScore = new HashMap<>();
        Map<Long, Set<Long>> filmFromUsers = new HashMap<>(); // для отладки

        for (UserSimilarity similarUser : topUsers) {
            for (Long filmId : similarUser.newFilms) {
                filmScore.put(filmId, filmScore.getOrDefault(filmId, 0) + 1);
                filmFromUsers.computeIfAbsent(filmId, k -> new HashSet<>()).add(similarUser.user.getId());
            }
        }

        // сортируем фильмы по весу (количеству похожих пользователей) и ID
        List<Film> recommendations = filmScore.entrySet().stream()
                .sorted((e1, e2) -> {
                    int compare = Integer.compare(e2.getValue(), e1.getValue()); // по убыванию веса
                    if (compare == 0) {
                        return Long.compare(e1.getKey(), e2.getKey()); // по ID
                    }
                    return compare;
                })
                .map(entry -> getFilmById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Найдено рекомендаций: {} (из {} уникальных фильмов)",
                recommendations.size(), filmScore.size());

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

    private Map<Long, Set<Long>> loadAllUserLikes() {
        String sql = "SELECT user_id, film_id FROM film_likes";
        Map<Long, Set<Long>> userLikesMap = new HashMap<>();

        jdbc.query(sql, (rs) -> {
            Long userId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");
            userLikesMap.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return userLikesMap;
    }

    private static class UserSimilarity {
        private final User user;
        private final int commonCount;
        private final Set<Long> newFilms; // фильмы, которые есть у user, но нет у target

        public UserSimilarity(User user, int commonCount, Set<Long> newFilms) {
            this.user = user;
            this.commonCount = commonCount;
            this.newFilms = newFilms;
        }
    }
}
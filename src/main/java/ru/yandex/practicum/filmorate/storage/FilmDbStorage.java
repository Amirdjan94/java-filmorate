package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
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

    @Override
    public Collection<Film> getUserRecommendations(User targetUser, Collection<User> allUsers) {
        log.info("Поиск рекомендаций для пользователя с ID-" + targetUser.getId() + " используя Slope One");

        if (getFilms().isEmpty()) {
            throw new ObjectNotFoundException("Фильмы не найдены");
        }

        // 1. Получаем все фильмы и их ID
        List<Film> allFilms = getFilms();
        Set<Long> allFilmIds = allFilms.stream().map(Film::getId).collect(Collectors.toSet());

        // солучаем все лайки пользователей из БД
        String getAllLikesSql = "SELECT user_id, film_id FROM film_likes";
        Map<Long, Set<Long>> userLikesMap = new HashMap<>();
        jdbc.query(getAllLikesSql,
                (rs) -> {
                    Long userId = rs.getLong("user_id");
                    Long filmId = rs.getLong("film_id");
                    userLikesMap.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
                }
        );

        // строим карту рейтингов: пользователь -> (фильм -> лайк)
        Map<Long, Map<Long, Double>> userFilmRatings = new HashMap<>();
        for (User user : allUsers) {
            Map<Long, Double> filmRatings = new HashMap<>();
            Set<Long> userLikes = userLikesMap.getOrDefault(user.getId(), new HashSet<>());
            for (Long filmId : allFilmIds) {
                if (userLikes.contains(filmId)) {
                    filmRatings.put(filmId, 1.0);
                } else {
                    filmRatings.put(filmId, 0.0);
                }
            }
            userFilmRatings.put(user.getId(), filmRatings);
        }

        // строим матрицы различия и частоты
        Map<Long, Map<Long, Double>> diff = new HashMap<>(); // различия
        Map<Long, Map<Long, Integer>> freq = new HashMap<>(); // частота встречаемости

        // для каждого пользователя
        for (Map<Long, Double> ratings : userFilmRatings.values()) {
            // для каждой пары фильмов
            for (Map.Entry<Long, Double> filmOne : ratings.entrySet()) {
                Long filmOneId = filmOne.getKey();
                Double filmRatingOne = filmOne.getValue();

                diff.putIfAbsent(filmOneId, new HashMap<>());
                freq.putIfAbsent(filmOneId, new HashMap<>());

                for (Map.Entry<Long, Double> filmTwo : ratings.entrySet()) {
                    Long filmTwoId = filmTwo.getKey();
                    Double filmRatingTwo = filmTwo.getValue();

                    // вычисляем разницу
                    double observedDiff = filmRatingOne - filmRatingTwo;

                    // обновляем частоту
                    int oldCount = freq.get(filmOneId).getOrDefault(filmTwoId, 0);
                    freq.get(filmOneId).put(filmTwoId, oldCount + 1);

                    // обновляем сумму разниц
                    double oldDiff = diff.get(filmOneId).getOrDefault(filmTwoId, 0.0);
                    diff.get(filmOneId).put(filmTwoId, observedDiff + oldDiff);
                }
            }
        }

        // усредняем значения
        for (Long filmOneId : diff.keySet()) {
            for (Long filmTwoId : diff.get(filmOneId).keySet()) {
                double sumDiff = diff.get(filmOneId).get(filmTwoId);
                int count = freq.get(filmOneId).get(filmTwoId);
                diff.get(filmOneId).put(filmTwoId, sumDiff / count);
            }
        }

        // делаем предсказания для целевого пользователя
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> predictionsFreq = new HashMap<>();

        Map<Long, Double> targetUserRatings = userFilmRatings.get(targetUser.getId());

        // обрабатываем каждый фильм, который оценил пользователь
        for (Map.Entry<Long, Double> targetEntry : targetUserRatings.entrySet()) {
            Long targetFilmId = targetEntry.getKey();
            Double targetRating = targetEntry.getValue();

            // обработка для каждого фильма, который мы хотим предсказать
            for (Long filmId : allFilmIds) {
                if (targetFilmId.equals(filmId)) {
                    continue; // пропускаем одинаковые фильмы
                }

                if (diff.containsKey(filmId) && diff.get(filmId).containsKey(targetFilmId)) {
                    // используем формулу predicted = diff[film][target] + rating[target]
                    double predictedValue = diff.get(filmId).get(targetFilmId) + targetRating;
                    int count = freq.get(filmId).get(targetFilmId);

                    predictions.put(filmId, predictions.getOrDefault(filmId, 0.0) + predictedValue * count);
                    predictionsFreq.put(filmId, predictionsFreq.getOrDefault(filmId, 0) + count);
                }
            }
        }

        // усредняем предсказания
        Map<Long, Double> finalPredictions = new HashMap<>();
        for (Long filmId : predictions.keySet()) {
            double sum = predictions.get(filmId);
            int count = predictionsFreq.get(filmId);
            if (count > 0) {
                finalPredictions.put(filmId, sum / count);
            }
        }

        // оставляем только те фильмы, которые пользователь не лайкнул, и сортируем по убыванию
        Set<Long> targetUserLikedFilms = allFilms.stream()
                .filter(film -> {
                    // безопасная проверка на null
                    Set<Long> filmLikes = film.getLikes();
                    return filmLikes != null && filmLikes.contains(targetUser.getId());
                })
                .map(Film::getId)
                .collect(Collectors.toSet());

        List<Film> recommendations = finalPredictions.entrySet().stream()
                .filter(entry -> !targetUserLikedFilms.contains(entry.getKey()))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // по убыванию
                .map(entry -> getFilmById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Найдено рекомендаций: " + recommendations.size());
        return recommendations;
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

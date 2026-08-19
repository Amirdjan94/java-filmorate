package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.EventOperation;
import ru.yandex.practicum.filmorate.data.EventType;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.data.Constants.FIRST_FILM_RELEASE_DATE;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenresService genresService;
    private final UserService userService;
    @Autowired
    private FeedService feedService;
    private final DirectorService directorService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
                       @Qualifier("directorService") DirectorService directorService,
                       @Qualifier("genresService") GenresService genresService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaStorage = mpaStorage;
        this.genresService = genresService;
        this.directorService = directorService;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film create(Film film) {
        validateAndNormalizeFields(film);
        checkMpa(film);
        checkGenres(film);
        checkDirectors(film);
        log.info("Creating film with genres: {}", film.getGenres());
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        normalizeFields(film);
        checkMpa(film);
        checkGenres(film);
        checkDirectors(film);
        Film currentFilm = getFilmById(film.getId());
        return filmStorage.update(film, currentFilm);
    }

    public Film getFilmById(Long id) {
        checkFilmsId(id);
        Optional<Film> film = filmStorage.getFilmById(id);
        if (film.isEmpty()) {
            throw new ObjectNotFoundException("Фильм с id=" + id + " не найден");
        }
        return film.get();
    }

    public Map<String, String> addLike(Long filmId, Long userId) { // добавление лайка
        log.info("Получили запрос на добавление лайка в фильм с ID-" + filmId + " пользователем с ID-" + userId);
        User user = userService.getUserById(userId); // Если пользователя нет по указанному ID или не валидный ID, будет выброшен exception
        Film film = getFilmById(filmId);
        filmStorage.addLike(film, user);
        log.info("Лайк успешно добавлен");
        feedService.addFeed(filmId, userId, EventType.LIKE, EventOperation.ADD);
        return Map.of(
                "operation", "Add new like"
        );
    }

    public Map<String, String> deleteLike(Long filmId, Long userId) { // удаление лайка
        log.info("Получили запрос на удаление лайка из фильма с ID-" + filmId + " пользователем с ID-" + userId);
        User user = userService.getUserById(userId); // Если пользоваеля нет по указанному ID или не валидный ID, будет выброшен exception
        Film film = getFilmById(filmId);
        if (filmStorage.deleteLike(film, user)) {
            feedService.addFeed(filmId, userId, EventType.LIKE, EventOperation.REMOVE);
            return Map.of(
                    "status", "success",
                    "operation", "Delete like"
            );
        } else {
            throw new ConditionsNotMetException("Film don't have like for this user");
        }
    }

    public Collection<Film> getMostPopularFilms(int count) { // возвращает список из первых count фильмов
        // по количеству лайков.
        if (count <= 0) {
            throw new ConditionsNotMetException("count должен быть больше нуля");
        }
        return filmStorage.getMostPopularFilms(count);
    }

    public Collection<Film> getCommonUserFilms(Long userId, Long friendId) {
        userService.getUserById(userId);
        userService.getUserById(friendId);

        return filmStorage.getCommonUserFilms(userId, friendId);
    }

    private void validateAndNormalizeFields(Film film) {
        releaseDateValidator(film);
        normalizeFields(film);
    }

    private void normalizeFields(Film film) {
        if (film.getName() != null) {
            film.setName(film.getName().trim());
        }
        if (film.getDescription() != null) {
            film.setDescription(film.getDescription().trim());
        }
    }

    private void releaseDateValidator(Film film) {
        log.debug("Ввалидация поля releaseDate");
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            log.warn("Дата релиза " + film.getReleaseDate());
            throw new ConditionsNotMetException("Дата релиза — не раньше " + FIRST_FILM_RELEASE_DATE);
        }
        log.debug("Поле releaseDate валиден");
    }

    private void checkFilmsId(Long filmId) {
        if (filmId == null || filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
    }

    private void checkGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genres> fullGenres = genresService.getGenresListById(film.getGenres());
            if (fullGenres.size() != film.getGenres().size()) {
                throw new ObjectNotFoundException("Передан не существующий жанр");
            }

            Map<Long, Genres> genresMap = fullGenres.stream()
                    .collect(Collectors.toMap(Genres::getId, g -> g));
            Set<Genres> updatedGenres = film.getGenres().stream()
                    .map(g -> genresMap.get(g.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            film.setGenres(updatedGenres);
        }
    }

    private void checkMpa(Film film) {
        if (film.getMpa() == null || mpaStorage.getMpaById(film.getMpa().getId()).isEmpty()) {
            throw new ObjectNotFoundException("Не найден рейтинг MPA по указанному id");
        }
    }

    public List<Film> getPopular(int count,
                                 Integer genreId,
                                 Integer year) {

        if (count <= 0) {
            throw new ConditionsNotMetException(
                    "count должен быть больше нуля"
            );
        }

        if (genreId != null) {
            genresService.getGenresById(
                    Long.valueOf(genreId)
            );
        }

        if (year != null) {

            if (year < FIRST_FILM_RELEASE_DATE.getYear()) {
                throw new ConditionsNotMetException(
                        "Год не может быть раньше " +
                                FIRST_FILM_RELEASE_DATE.getYear()
                );
            }

            if (year > java.time.LocalDate.now().getYear()) {
                throw new ConditionsNotMetException(
                        "Год не может быть больше текущего"
                );
            }
        }

        return filmStorage.getPopular(
                count,
                genreId,
                year
        );
    }

    public Collection<Film> getByDirector(Long directorId, String sortBy) {
        directorService.findById(directorId);
        return filmStorage.getByDirector(directorId, sortBy);
    }

    private void checkDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                if (director.getId() == null) {
                    throw new ConditionsNotMetException("ID режиссёра не может быть null");
                }
                directorService.findById(director.getId());
            }
        }
    }

    public List<Film> searchFilms(String query, String by) {
        // Нормализация
        query = query.trim();
        by = by.trim().replaceAll("\\s+", "");

        if (query.isBlank()) {
            throw new ConditionsNotMetException("Query не может быть пустым");
        }
        if (by.isBlank()) {
            throw new ConditionsNotMetException("By не может быть пустым");
        }

        String byLower = by.toLowerCase();
        if (!byLower.contains("title") && !byLower.contains("director")) {
            throw new ConditionsNotMetException("By должен содержать 'title' и/или 'director'");
        }

        return filmStorage.searchFilms(query, by);
    }

    public void deleteFilm(long filmId) {
        getFilmById(filmId);
        filmStorage.deleteFilm(filmId);
        log.info("Фильм с id={} удалён", filmId);
    }
}

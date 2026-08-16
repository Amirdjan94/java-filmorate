package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.data.Constants.FIRST_FILM_RELEASE_DATE;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenresService genresService;
    private final UserService userService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
                       @Qualifier("genresService") GenresService genresService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaStorage = mpaStorage;
        this.genresService = genresService;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film create(Film film) {
        validateAndNormalizeFields(film);
        checkMpa(film);
        checkGenres(film);
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
        return Map.of(
                "operation", "Add new like"
        );
    }

    public Map<String, String> deleteLike(Long filmId, Long userId) { // удаление лайка
        log.info("Получили запрос на удаление лайка из фильма с ID-" + filmId + " пользователем с ID-" + userId);
        User user = userService.getUserById(userId); // Если пользоваеля нет по указанному ID или не валидный ID, будет выброшен exception
        Film film = getFilmById(filmId);
        if (filmStorage.deleteLike(film, user)) {
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
            throw new ConditionsNotMetException("Дата релиза — не раньше "
                    + FIRST_FILM_RELEASE_DATE);
        }
        log.debug("Поле releaseDate валиден");
    }

    private void checkFilmsId(Long filmId) {
        if (filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
    }

    private void checkGenres(Film film) {
        if (film.getGenres() != null) {
            if (genresService.getGenresListById(film.getGenres()).size() != film.getGenres().size()) {
                throw new ObjectNotFoundException("Передан не существующий жанр");
            }
        }
    }

    private void checkMpa(Film film) {
        if (film.getMpa() == null || mpaStorage.getMpaById(film.getMpa().getId()).isEmpty()) {
            throw new ObjectNotFoundException("Не найден рейтинг MPA по указанному id");
        }
    }
}

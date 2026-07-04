package ru.yandex.practicum.filmorate.controller;

import com.sun.jdi.request.DuplicateRequestException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.data.Constants.FIRST_FILM_RELEASE_DATE;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получили запрос на список фильмов");
        log.info("Передали список фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получили запрос на добавление фильма");
        log.debug("Входящий запрос - " + film.toString());
        validateAndNormalizeFields(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("В список добавили фильм - " + film.toString());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получили запрос на обновление информации фильме");
        log.debug("Входящий запрос - " + film.toString());
        if (film.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Отсутсвует фильм с указанным Id");
            throw new ConditionsNotMetException("Отсутсвует фильм с указанным Id");
        }
        normalizeFields(film);
        Film currentFilm = films.get(film.getId());
        if (film.getName() != null) {
            currentFilm.setName(film.getName());
        }
        if (film.getReleaseDate() != null) {
            currentFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDescription() != null) {
            releaseDateValidator(film);
            currentFilm.setDescription(film.getDescription());
        }
        if (film.getDuration() != null) {
            currentFilm.setDuration(film.getDuration());
        }
        log.info("Информация о фильме успешно обновлено");
        return currentFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateAndNormalizeFields(Film film) {
        checkDuplicateFilms(film);
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

    private void checkDuplicateFilms(Film film) {
        if (films.values().contains(film)) {
            log.warn("Переданный фильм уже есть в списке");
            throw new DuplicateRequestException("Фильм уже есть в списках");
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

}

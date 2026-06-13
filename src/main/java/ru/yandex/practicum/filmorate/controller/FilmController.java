package ru.yandex.practicum.filmorate.controller;

import com.sun.jdi.request.DuplicateRequestException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.services.filmServices.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();
    private List<FilmValidateProcessor> filmValidateProcessors = List.of(new NameValidator(),
            new DescriptionValidator(), new ReleaseDateValidator(), new DurationValidator());

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
        if (films.values().contains(film)) {
            log.warn("Переданный фильм уже есть в списке");
            throw new DuplicateRequestException("Фильм уже есть в списках");
        }
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
            new NameValidator().doValidate(film);
            currentFilm.setName(film.getName());
        }
        if (film.getReleaseDate() != null) {
            new ReleaseDateValidator().doValidate(film);
            currentFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDescription() != null) {
            new DescriptionValidator().doValidate(film);
            currentFilm.setDescription(film.getDescription());
        }
        if (film.getDuration() != null) {
            new DurationValidator().doValidate(film);
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
        for (FilmValidateProcessor processorName : filmValidateProcessors) {
            processorName.doValidate(film);
        }
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

}

package ru.yandex.practicum.filmorate.storage;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    public Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        log.info("Получили запрос на список фильмов");
        log.info("Передали список фильмов");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Получили запрос на добавление фильма");
        log.debug("Входящий запрос - " + film.toString());
        checkDuplicateFilms(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("В список добавили фильм - " + film.toString());
        return film;
    }

    @Override
    public Film update(Film newFilm, Film currentFilm) {
        log.info("Получили запрос на обновление информации фильме");
        log.debug("Входящий запрос - " + newFilm.toString());
        if (newFilm.getName() != null) {
            currentFilm.setName(newFilm.getName());
        }
        if (newFilm.getReleaseDate() != null) {
            currentFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDescription() != null) {
            currentFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getDuration() != null) {
            currentFilm.setDuration(newFilm.getDuration());
        }
        log.info("Информация о фильме успешно обновлено");
        return currentFilm;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        log.info("Получили запрос на передачу фильма с ID-" + id);
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getMostPopularFilms(int count) {
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        return getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void clearStorage() {
        films.clear();
    }

    private void checkDuplicateFilms(Film film) {
        if (films.values().contains(film)) {
            log.warn("Переданный фильм уже есть в списке");
            throw new DuplicateRequestException("Фильм уже есть в списках");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

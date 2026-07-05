package ru.yandex.practicum.filmorate.storage;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.data.Constants.FIRST_FILM_RELEASE_DATE;

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
        validateAndNormalizeFields(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("В список добавили фильм - " + film.toString());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Получили запрос на обновление информации фильме");
        log.debug("Входящий запрос - " + film.toString());
        if (film.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Отсутсвует фильм с указанным Id");
            throw new ObjectNotFoundException("Отсутсвует фильм с указанным Id");
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

    @Override
    public Film getFilmById(Long id) {
        log.info("Получили запрос на передачу фильма с ID-" + id);
        log.debug("Запуск валидации входных данных");
        checkFilmsId(id);
        log.debug("Корректные входные данные");
        log.info("Передали фильм по ID-" + id);
        return films.get(id);
    }

    public void clearStorage() {
        films.clear();
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

    private void checkFilmsId(Long filmId) {
        if (filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
        if (!films.containsKey(filmId)) {
            throw new ObjectNotFoundException("Пользователь с id=" + filmId + " не найден");
        }
    }
}

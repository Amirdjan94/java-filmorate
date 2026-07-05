package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FilmService {

    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public Map<String, String> addLike(Long filmId, Long userId) { // добавление лайка
        log.info("Получили запрос на добавление лайка в фильм с ID-" + filmId + " пользователем с ID-" + userId);
        User user = inMemoryUserStorage.getUserById(userId); // Если пользоваеля нет по указанному ID или не валидный ID, будет выброшен exception
        inMemoryFilmStorage.getFilmById(filmId).getLikes().add(user.getId());
        log.info("Лайк успешно добавлен");
        return Map.of(
                "status", "success",
                "operation", "Add new like"
        );
    }

    public Map<String, String> deleteLike(Long filmId, Long userId) { // удаление лайка
        log.info("Получили запрос на удаление лайка из фильма с ID-" + filmId + " пользователем с ID-" + userId);
        User user = inMemoryUserStorage.getUserById(userId); // Если пользоваеля нет по указанному ID или не валидный ID, будет выброшен exception
        if (inMemoryFilmStorage.getFilmById(filmId).getLikes().remove(user.getId())) {
            return Map.of(
                    "status", "success",
                    "operation", "Delete like"
            );
        } else {
            return Map.of(
                    "status", "false",
                    "operation", "Film don't have like for this user"
            );
        }
    }

    public Collection<Film> getMostPopularFilms(int count) { // возвращает список из первых count фильмов
        // по количеству лайков.
        Comparator<Film> comparator = Comparator.comparing(film -> film.getLikes().size());
        return inMemoryFilmStorage.getFilms().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}

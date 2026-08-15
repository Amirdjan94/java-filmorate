package ru.yandex.practicum.filmorate.storage;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component()
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

    @Override
    public void addLike(Film film, User user) { // добавление лайка
        film.getLikes().add(user.getId());
    }

    @Override
    public boolean deleteLike(Film film, User user) { // удаление лайка
        return film.getLikes().remove(user.getId());
    }

    Override
    public Collection<Film> getUserRecommendations(User targetUser, Collection<User> allUsers) {
        log.info("Поиск рекомендаций для пользователя с ID-" + targetUser.getId() + " на основе пересечений лайков");

        if (films.values().isEmpty()) {
            throw new ObjectNotFoundException("Фильмы не найдены");
        }

        // получаем фильмы, которые лайкнул целевой пользователь
        Set<Long> targetUserLikedFilms = films.values().stream()
                .filter(film -> {
                    Set<Long> filmLikes = film.getLikes();
                    return filmLikes != null && filmLikes.contains(targetUser.getId());
                })
                .map(Film::getId)
                .collect(Collectors.toSet());

        if (targetUserLikedFilms.isEmpty()) {
            log.info("У целевого пользователя нет лайков, рекомендации невозможны");
            return Collections.emptyList();
        }

        // находим ВСЕХ пользователей с пересечением по лайкам
        Map<User, Set<Long>> similarUsersWithNewFilms = new HashMap<>();
        Map<User, Integer> userSimilarityScore = new HashMap<>();

        for (User user : allUsers) {
            if (user.getId().equals(targetUser.getId())) {
                continue;
            }

            // Получаем фильмы, которые лайкнул этот пользователь
            Set<Long> userLikedFilms = films.values().stream()
                    .filter(film -> {
                        Set<Long> filmLikes = film.getLikes();
                        return filmLikes != null && filmLikes.contains(user.getId());
                    })
                    .map(Film::getId)
                    .collect(Collectors.toSet());

            if (userLikedFilms.isEmpty()) {
                continue;
            }

            // вычисляем пересечение (количество общих лайков)
            Set<Long> intersection = new HashSet<>(targetUserLikedFilms);
            intersection.retainAll(userLikedFilms);
            int commonCount = intersection.size();

            // если есть общие лайки
            if (commonCount > 0) {
                // определяем фильмы, которые один пролайкал, а другой нет
                Set<Long> newFilms = new HashSet<>(userLikedFilms);
                newFilms.removeAll(targetUserLikedFilms); // фильмы, которые есть у user, но нет у target

                if (!newFilms.isEmpty()) {
                    similarUsersWithNewFilms.put(user, newFilms);
                    userSimilarityScore.put(user, commonCount);
                }
            }
        }

        if (similarUsersWithNewFilms.isEmpty()) {
            log.info("Не найдено пользователей с общими лайками");
            return Collections.emptyList();
        }

        // сортируем ВСЕХ пользователей по максимальному пересечению
        List<User> sortedUsers = similarUsersWithNewFilms.keySet().stream()
                .sorted((u1, u2) -> Integer.compare(
                        userSimilarityScore.getOrDefault(u2, 0),
                        userSimilarityScore.getOrDefault(u1, 0)
                ))
                .collect(Collectors.toList());

        // используем ВСЕХ пользователей, а не только топ-5
        log.info("Найдено {} похожих пользователей", sortedUsers.size());

        // Собираем рекомендуемые фильмы со ВСЕХ похожих пользователей
        Map<Long, Integer> filmScore = new HashMap<>();
        for (User user : sortedUsers) { // ← теперь ВСЕ пользователи
            Set<Long> newFilms = similarUsersWithNewFilms.get(user);
            for (Long filmId : newFilms) {
                filmScore.put(filmId, filmScore.getOrDefault(filmId, 0) + 1);
            }
        }

        // рекомендуем фильмы, которые поставили лайк пользователи с похожими вкусами
        List<Film> recommendations = filmScore.entrySet().stream()
                .sorted((e1, e2) -> {
                    // Сортируем по количеству пользователей, которые рекомендуют фильм
                    int compare = Integer.compare(e2.getValue(), e1.getValue());
                    if (compare == 0) {
                        return Long.compare(e1.getKey(), e2.getKey());
                    }
                    return compare;
                })
                .map(entry -> getFilmById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Найдено рекомендаций: {} (из {} уникальных фильмов, рекомендованных {} похожими пользователями)",
                recommendations.size(), filmScore.size(), sortedUsers.size());

        return recommendations;
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

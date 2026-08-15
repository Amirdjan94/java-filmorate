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

    @Override
    public Collection<Film> getUserRecommendations(User targetUser, Collection<User> allUsers) {
        log.info("Поиск рекомендаций для пользователя с ID-" + targetUser.getId());

        if (films.values().isEmpty()) {
            throw new ObjectNotFoundException("Фильмы не найдены");
        }

        // собираем данные для пользователя - (фильм -лайк)
        Map<Long, Map<Long, Double>> userFilmRatings = new HashMap<>();
        for (User user : allUsers) {
            Map<Long, Double> filmRatings = new HashMap<>();
            for (Film film : films.values()) {
                if (film.getLikes().contains(user.getId())) {
                    filmRatings.put(film.getId(), 1.0);
                } else  {
                    filmRatings.put(film.getId(), 0.0);
                }
            }
            userFilmRatings.put(user.getId(), filmRatings);
        }

        //строим матрицы различия и частоты
        Map<Long, Map<Long, Double>> diff = new HashMap<>(); //различия
        Map<Long, Map<Long, Integer>> freq = new HashMap<>(); //частота встречаемости

        //теперь строим их для каждого пользователя
        for (Map<Long, Double> ratings : userFilmRatings.values()) {
            //теперь к каждому пользователю берем пары фильмов
            for (Map.Entry<Long, Double> filmOne : ratings.entrySet()) {
                //фильм первый
                Long filmOneId = filmOne.getKey();
                Double filmRatingOne = filmOne.getValue();

                diff.putIfAbsent(filmOneId, new HashMap<>());
                freq.putIfAbsent(filmOneId, new HashMap<>());

                for (Map.Entry<Long, Double> filmTwo : ratings.entrySet()) {
                    //фильм второй
                    Long filmTwoId = filmTwo.getKey();
                    Double filmRatingTwo = filmTwo.getValue();

                    //вычисляем разницу
                    double observedDiff = filmRatingOne - filmRatingTwo;

                    //обновляем частоту
                    int oldCount = freq.get(filmOneId).getOrDefault(filmTwoId, 0);
                    freq.get(filmOneId).put(filmTwoId, oldCount + 1);

                    //обновляем сумму разниц
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

        //делаем предсказания для целевого пользователя
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> predictionsFreq = new HashMap<>();

        Map<Long, Double> targetUserRatings = userFilmRatings.get(targetUser.getId());

        //обрабатываем каждый фильм который оценил пользователь
        for (Map.Entry<Long, Double> targetEntry : targetUserRatings.entrySet()) {
            Long targetFilmId = targetEntry.getKey();
            Double targetRating = targetEntry.getValue();

            //обработка для каждого фильма который мы хотим предсказать
            for (Long filmId : films.keySet().stream().collect(Collectors.toList())) {
                if (targetFilmId.equals(filmId)) {
                    continue; // пропускаем одинаковые фильмы
                }

                if (diff.containsKey(filmId) && diff.get(filmId).containsKey(targetFilmId)) {

                    //Используем формулу predicted = diff[film][target] + rating[target]
                    double predictedValue = diff.get(filmId).get(targetFilmId) + targetRating;
                    int count = freq.get(filmId).get(targetFilmId);

                    predictions.put(filmId, predictions.getOrDefault(filmId,0.0) + predictedValue * count);
                    predictionsFreq.put(filmId, predictionsFreq.getOrDefault(filmId,0) + count);
                }
            }
        }

        //усредняем предсказания
        Map<Long, Double> finalPredictions = new HashMap<>();
        for (Long filmId : predictions.keySet()) {
            double sum = predictions.get(filmId);
            int count = predictionsFreq.get(filmId);
            if (count > 0) {
                finalPredictions.put(filmId, sum / count);
            }
        }

        //осталвяем только те фильмы которые пользователь не лайкнул и сортируем по убыванию
        Set<Long> targetUserLikedFilms = films.values().stream()
                .filter(film -> film.getLikes().contains(targetUser.getId()))
                .map(Film::getId)
                .collect(Collectors.toSet());

        List<Film> recommendations = finalPredictions.entrySet().stream()
                .filter(entry -> !targetUserLikedFilms.contains(entry.getKey()))
                .sorted((e1, e2) -> Double.compare(e1.getValue(), e2.getValue()))
                .map(entry -> getFilmById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Найдено рекомендаций: " + recommendations.size());
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

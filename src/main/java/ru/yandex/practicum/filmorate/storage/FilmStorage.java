package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm, Film currentFilm);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(Long id);

    Collection<Film> getMostPopularFilms(int count);

    void addLike(Film film, User user);

    boolean deleteLike(Film film, User user);

    Collection<Film> getUserRecommendations(User user, Collection<User> users);

    Collection<Film> getCommonUserFilms(Long userId, Long friendId);

    List<Film> getPopular(int count, Integer genreId, Integer year);

    Collection<Film> getByDirector(Long id, String sortBy);

    void deleteFilm(long filmId);
}

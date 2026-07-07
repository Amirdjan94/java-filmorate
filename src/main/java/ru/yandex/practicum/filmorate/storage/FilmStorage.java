package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm, Film currentFilm);

    Collection<Film> getFilms();

    Optional<Film> getFilmById(Long id);

    Collection<Film> getMostPopularFilms(int count);
}

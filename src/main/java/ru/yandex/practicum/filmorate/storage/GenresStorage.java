package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.Collection;
import java.util.Optional;

public interface GenresStorage {

    Collection<Genres> getGenres();

    Optional<Genres> getGenresById(Long genresId);

}

package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenresStorage {

    Collection<Genres> getGenres();

    Optional<Genres> getGenresById(Long genresId);

    List<Genres> getGenresListById(Set<Genres> setGenres);

}

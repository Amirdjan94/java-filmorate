package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class GenresService {

    private final GenresStorage genresStorage;

    public GenresService(@Qualifier("genresDbStorage") GenresStorage genresStorage) {
        this.genresStorage = genresStorage;
    }

    public Collection<Genres> getGenres() {
        return genresStorage.getGenres();
    }

    public Genres getGenresById(Long id) {
        checkGenresId(id);
        Optional<Genres> genres = genresStorage.getGenresById(id);
        if (genres.isEmpty()) {
            throw new ObjectNotFoundException("Жанр с id=" + id + " не найден");
        }
        return genres.get();
    }

    private void checkGenresId(Long filmId) {
        if (filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
    }

    public List<Genres> getGenresListById(Set<Genres> setGenres) {
        return genresStorage.getGenresListById(setGenres);
    }
}

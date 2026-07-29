package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenresMapper;
import ru.yandex.practicum.filmorate.dto.GenresDto;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GenresService {

    private final GenresStorage genresStorage;

    public GenresService(@Qualifier("genresDbStorage") GenresStorage genresStorage) {
        this.genresStorage = genresStorage;
    }

    public Collection<GenresDto> getGenres() {
        return genresStorage.getGenres().stream().map(GenresMapper::mapToGenresDto).collect(Collectors.toList());
    }

    public GenresDto getGenresById(Long id) {
        checkGenresId(id);
        Optional<Genres> genres = genresStorage.getGenresById(id);
        if (genres.isEmpty()) {
            throw new ObjectNotFoundException("Жанр с id=" + id + " не найден");
        }
        return GenresMapper.mapToGenresDto(genres.get());
    }

    private void checkGenresId(Long filmId) {
        if (filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
    }
}

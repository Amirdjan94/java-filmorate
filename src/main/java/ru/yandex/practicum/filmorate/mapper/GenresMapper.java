package ru.yandex.practicum.filmorate.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenresDto;
import ru.yandex.practicum.filmorate.model.Genres;

@Component
@NoArgsConstructor
public class GenresMapper {

    public static GenresDto mapToGenresDto(Genres genres) {
        GenresDto genresDto = new GenresDto();
        genresDto.setId(genres.getGenreId());
        genresDto.setName(genres.getGenreName());
        return genresDto;
    }
}
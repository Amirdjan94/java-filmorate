package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genres;

import java.util.*;

@Repository
@Slf4j
public class GenresDbStorage extends BaseRepository<Genres> implements GenresStorage {
    private static final String FIND_ALL_GENRES = "SELECT * FROM genres";
    private static final String FIND_GENRES_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";

    public GenresDbStorage(JdbcTemplate jdbc, RowMapper<Genres> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genres> getGenres() {
        return findMany(FIND_ALL_GENRES);
    }

    @Override
    public Optional<Genres> getGenresById(Long genresId) {
        return findOne(FIND_GENRES_BY_ID, genresId);
    }
}

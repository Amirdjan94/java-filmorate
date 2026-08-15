package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {
    private static final String FIND_ALL_MPA = "SELECT * FROM rating_mpa ORDER BY rating_mpa_id";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM rating_mpa WHERE ratingMpaId = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Mpa> getMpaList() {
        return findMany(FIND_ALL_MPA);
    }

    @Override
    public Optional<Mpa> getMpaById(Long mpaId) {
        return findOne(FIND_MPA_BY_ID, mpaId);
    }

}

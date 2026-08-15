package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.excepton.InternalServerException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {
    private static final String FIND_ALL_DIRECTORS = "SELECT * FROM directors";
    private static final String FIND_DIRECTOR_BY_ID = "SELECT * FROM directors WHERE director_id = ?";
    private static final String CREATE_DIRECTOR = "INSERT INTO directors(name, lastname) VALUES(?, ?)";
    private static final String UPDATE_DIRECTOR = "UPDATE directors SET name = ?, lastname = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR = "DELETE FROM directors WHERE director_id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_DIRECTORS);
    }

    @Override
    public Optional<Director> findById(Long id) {
        return findOne(FIND_DIRECTOR_BY_ID, id);
    }

    @Override
    public Director create(Director director) {
        long id = insert(CREATE_DIRECTOR,
                director.getName(),
                director.getLastname()
        );
        director.setId(id);
        return director;

    }

    @Override
    public Director update(Director director) {
        update(UPDATE_DIRECTOR,
                director.getName(),
                director.getLastname(),
                director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        boolean deleted = delete(DELETE_DIRECTOR, id);
        if (!deleted) {
            throw new InternalServerException("Не удалось удалить режиссёра с id=" + id);
        }
    }
}

package ru.yandex.practicum.filmorate.db.genres;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.GenresDbStorage;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class GenresDbGetGenresMethodTest {
    private final GenresDbStorage genresDbStorage;

    @Test
    public void getMpaList_returnsMpaList() {
        Collection<Genres> genresCollection = genresDbStorage.getGenres();
        assertTrue(!genresCollection.isEmpty());
        assertTrue(genresCollection.contains(genresDbStorage.getGenresById(1L).get()));
    }
}

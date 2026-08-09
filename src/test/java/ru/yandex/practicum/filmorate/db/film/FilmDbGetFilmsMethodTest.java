package ru.yandex.practicum.filmorate.db.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class FilmDbGetFilmsMethodTest {
    private final FilmDbStorage filmStorage;
    private Film film;

    @BeforeEach
    void beforeEach() {
        filmStorage.deleteAllFilms();
    }

    @Test
    public void getFilms_notEmptyListOfFilm_returnsFilm() {
        film = filmStorage.create(Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa(1L, "тест"))
                .build()
        );
        Collection<Film> filmCollection = filmStorage.getFilms();
        assertThat(filmCollection)
                .anySatisfy(f -> {
                    assertThat(f.getName()).isEqualTo(film.getName());
                });
    }

    @Test
    public void getFilms_emptyListOfFilms_returnsEmptyList() {
        Collection<Film> filmCollection = filmStorage.getFilms();
        assertTrue(filmCollection.isEmpty());
    }
}

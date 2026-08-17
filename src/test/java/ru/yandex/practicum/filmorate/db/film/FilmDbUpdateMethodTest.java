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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class FilmDbUpdateMethodTest {
    private final FilmDbStorage filmStorage;
    private Film newFilm;
    private Film updateFilm;

    @BeforeEach
    void beforeEach() {
        filmStorage.deleteAllFilms();
        newFilm = filmStorage.create(Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build()
        );
    }

    @Test
    public void update_updateName_returnsFilmWithNewName() {
        updateFilm = Film.builder()
                .name("update New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa(1L, "тест"))
                .build();
        updateFilm = filmStorage.update(updateFilm, newFilm);
        assertThat(updateFilm).hasFieldOrPropertyWithValue("name", "update New film");
    }

    @Test
    public void update_updateDescription_returnsFilmWithNewDescription() {
        updateFilm = Film.builder()
                .name("New film")
                .description("update Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa(1L, "тест"))
                .build();
        updateFilm = filmStorage.update(updateFilm, newFilm);
        assertThat(updateFilm).hasFieldOrPropertyWithValue("description", "update Good new film");
    }

    @Test
    public void update_updaterReleaseDate_returnsFilmWithNewReleaseDate() {
        updateFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 23))
                .duration(100)
                .mpa(new Mpa(1L, "тест"))
                .build();
        updateFilm = filmStorage.update(updateFilm, newFilm);
        assertThat(updateFilm).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1994, 07, 23));
    }

    @Test
    public void update_updaterDuration_returnsFilmWithNewDuration() {
        updateFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 23))
                .duration(101)
                .mpa(new Mpa(1L, "тест"))
                .build();
        updateFilm = filmStorage.update(updateFilm, newFilm);
        assertThat(updateFilm).hasFieldOrPropertyWithValue("duration", 101);
    }
}

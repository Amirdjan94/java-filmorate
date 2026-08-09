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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class FilmDbGetFilmByIdMethodTest {
    private final FilmDbStorage filmDbStorage;
    private Film film;

    @BeforeEach
    void beforeEach() {
        filmDbStorage.deleteAllFilms();
        film = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa(1L, "тест"))
                .build();

        film = filmDbStorage.create(film);
        System.out.println(film);
    }

    @Test
    public void getUser_existFilmId_returnsFilm() {
        Optional<Film> filmOptional = filmDbStorage.getFilmById(film.getId());
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", film.getId())
                );
    }

    @Test
    public void getUser_notExistFilmId_returnsEmptyOptional() {
        Optional<Film> userOptional = filmDbStorage.getFilmById(film.getId() + 1);
        assertThat(userOptional)
                .isEmpty();
    }
}

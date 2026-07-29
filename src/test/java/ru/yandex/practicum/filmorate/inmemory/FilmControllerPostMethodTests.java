package ru.yandex.practicum.filmorate.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerPostMethodTests {
    InMemoryFilmStorage inMemoryFilmStorage;

    @BeforeEach
    void beforeEach() {
        inMemoryFilmStorage = new InMemoryFilmStorage();
        inMemoryFilmStorage.clearStorage();
    }

    @Test
    void create_validFilm_returnsFilm() {
        Film validFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(inMemoryFilmStorage.create(validFilm), validFilm);
    }

    @Test
    void create_descriptionHave200Symbols_returnsFilm() {
        Film descriptionHave200SymbolsFilm = Film.builder()
                .name("New film")
                .description("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят " +
                        "разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. " +
                        "о Куглов, который за время «сво")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(inMemoryFilmStorage.create(descriptionHave200SymbolsFilm), descriptionHave200SymbolsFilm);
    }

    @Test
    void create_filmreleaseDate1895_12_28_returnsFilm() {
        Film filmEmptyName = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();
        assertEquals(inMemoryFilmStorage.create(filmEmptyName), filmEmptyName);
    }

    @Test
    void create_filmDurationGreaterZero_returnsFilm() {
        Film filmDurationGreaterZero = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1995, 12, 27))
                .duration(1)
                .build();
        assertEquals(inMemoryFilmStorage.create(filmDurationGreaterZero), filmDurationGreaterZero);
        assertFalse(inMemoryFilmStorage.getFilms().isEmpty(), "Ожидается НЕ пустой список");
    }
}

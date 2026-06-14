package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerPostMethodTests {
    FilmController filmController;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    void create_validFilm_returnsFilm() {
        Film validFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.create(validFilm), validFilm);
        assertFalse(filmController.getFilms().isEmpty(), "Ожидается НЕ пустой список");
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
        assertEquals(filmController.create(descriptionHave200SymbolsFilm), descriptionHave200SymbolsFilm);
        assertFalse(filmController.getFilms().isEmpty(), "Ожидается НЕ пустой список");
        ;
    }

    @Test
    void create_filmreleaseDateBefore1895_12_28_returnsException() {
        Film filmEmptyName = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.create(filmEmptyName));
        assertTrue(filmController.getFilms().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_filmreleaseDate1895_12_28_returnsFilm() {
        Film filmEmptyName = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();
        assertEquals(filmController.create(filmEmptyName), filmEmptyName);
        assertFalse(filmController.getFilms().isEmpty(), "Ожидается НЕ пустой список");
    }

    @Test
    void create_filmDurationGreaterZero_returnsFilm() {
        Film filmDurationGreaterZero = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1995, 12, 27))
                .duration(1)
                .build();
        assertEquals(filmController.create(filmDurationGreaterZero), filmDurationGreaterZero);
        assertFalse(filmController.getFilms().isEmpty(), "Ожидается НЕ пустой список");
    }
}

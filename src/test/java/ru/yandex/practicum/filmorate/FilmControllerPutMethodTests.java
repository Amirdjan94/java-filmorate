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
class FilmControllerPutMethodTests {
    FilmController filmController;
    Film validFilm;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
        validFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        filmController.create(validFilm);
    }

    @Test
    void update_validNewName_returnsUpdateFilm() {
        Film updateValidFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.update(updateValidFilm), updateValidFilm);
        assertTrue(filmController.getFilms().toString().contains("New name"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_notExistId_returnsCurrentFilm() {
        Film updatenotExistIdFilm = Film.builder()
                .id(2L)
                .name("New name")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.update(updatenotExistIdFilm));
        assertFalse(filmController.getFilms().toString().contains("New name"), "Ожидается список без новых значений");
    }

    @Test
    void update_withoutName_returnsCurrentFilm() {
        Film updatenotExistIdFilm = Film.builder()
                .id(1L)
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.update(updatenotExistIdFilm), validFilm);
        assertTrue(filmController.getFilms().toString().contains("New film"), "Ожидается список без новых значений");
    }

    @Test
    void update_validNewDescription_returnsUpdateFilm() {
        Film validNewDescriptionFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.update(validNewDescriptionFilm), validNewDescriptionFilm);
        assertTrue(filmController.getFilms().toString().contains("New description"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutDescription_returnsCurrentFilm() {
        Film withoutDescriptionFilm = Film.builder()
                .id(1L)
                .name("New name")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.update(withoutDescriptionFilm), validFilm);
        assertFalse(filmController.getFilms().toString().contains("New description"), "Ожидается список без новых значений");
    }

    @Test
    void update_descriptionHave200Symbols_returnsUpdateFilm() {
        Film descriptionHave200SymbolsFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят " +
                        "разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. " +
                        "о Куглов, который за время «сво")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(filmController.update(descriptionHave200SymbolsFilm), descriptionHave200SymbolsFilm);
        assertTrue(filmController.getFilms().toString().contains("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят " +
                "разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. " +
                "о Куглов, который за время «сво"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_validNewDuration_returnsUpdateFilm() {
        Film validNewDurationFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(101)
                .build();
        assertEquals(filmController.update(validNewDurationFilm), validNewDurationFilm);
        assertTrue(filmController.getFilms().toString().contains("101"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutDuration_returnsCurrentFilm() {
        Film withoutDurationFilm = Film.builder()
                .id(1L)
                .name("New name")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .build();
        assertEquals(filmController.update(withoutDurationFilm), validFilm);
        assertTrue(filmController.getFilms().toString().contains("100"), "Ожидается список без новых значений");
    }

    @Test
    void update_validNewReleaseDate_returnsUpdateFilm() {
        Film validNewReleaseDateFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.of(1994, 07, 23))
                .duration(101)
                .build();
        assertEquals(filmController.update(validNewReleaseDateFilm), validNewReleaseDateFilm);
        assertTrue(filmController.getFilms().toString().contains("1994-07-23"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutReleaseDate_returnsCurrentFilm() {
        Film withoutReleaseDateFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .duration(101)
                .build();
        assertEquals(filmController.update(withoutReleaseDateFilm), validFilm);
        assertTrue(filmController.getFilms().toString().contains("1994-07-22"), "Ожидается список без новых значений");
    }

    @Test
    void update_filmReleaseDateBefore1895_12_28_returnsCurrentFilm() {
        Film releaseDateBefore1895Film = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100)
                .build();
        assertThrows(ConditionsNotMetException.class, () -> filmController.create(releaseDateBefore1895Film));
        assertTrue(filmController.getFilms().toString().contains("1994-07-22"), "Ожидается список без новых значений");
    }
}

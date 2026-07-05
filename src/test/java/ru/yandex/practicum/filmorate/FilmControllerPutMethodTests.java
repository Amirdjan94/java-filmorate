package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerPutMethodTests {
    InMemoryFilmStorage inMemoryFilmStorage;
    Film validFilm;

    @BeforeEach
    void beforeEach() {
        inMemoryFilmStorage = new InMemoryFilmStorage();
        validFilm = Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        inMemoryFilmStorage.create(validFilm);
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
        assertEquals(inMemoryFilmStorage.update(updateValidFilm), updateValidFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("New name"), "Ожидается список с новыми значениями");
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
        assertThrows(ObjectNotFoundException.class, () -> inMemoryFilmStorage.update(updatenotExistIdFilm));
        assertFalse(inMemoryFilmStorage.getFilms().toString().contains("New name"), "Ожидается список без новых значений");
    }

    @Test
    void update_withoutName_returnsCurrentFilm() {
        Film updatenotExistIdFilm = Film.builder()
                .id(1L)
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(inMemoryFilmStorage.update(updatenotExistIdFilm), validFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("New film"), "Ожидается список без новых значений");
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
        assertEquals(inMemoryFilmStorage.update(validNewDescriptionFilm), validNewDescriptionFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("New description"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutDescription_returnsCurrentFilm() {
        Film withoutDescriptionFilm = Film.builder()
                .id(1L)
                .name("New name")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .build();
        assertEquals(inMemoryFilmStorage.update(withoutDescriptionFilm), validFilm);
        assertFalse(inMemoryFilmStorage.getFilms().toString().contains("New description"), "Ожидается список без новых значений");
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
        assertEquals(inMemoryFilmStorage.update(descriptionHave200SymbolsFilm), descriptionHave200SymbolsFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят " +
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
        assertEquals(inMemoryFilmStorage.update(validNewDurationFilm), validNewDurationFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("101"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutDuration_returnsCurrentFilm() {
        Film withoutDurationFilm = Film.builder()
                .id(1L)
                .name("New name")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .build();
        assertEquals(inMemoryFilmStorage.update(withoutDurationFilm), validFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("100"), "Ожидается список без новых значений");
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
        assertEquals(inMemoryFilmStorage.update(validNewReleaseDateFilm), validNewReleaseDateFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("1994-07-23"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_withoutReleaseDate_returnsCurrentFilm() {
        Film withoutReleaseDateFilm = Film.builder()
                .id(1L)
                .name("New name")
                .description("New description")
                .duration(101)
                .build();
        assertEquals(inMemoryFilmStorage.update(withoutReleaseDateFilm), validFilm);
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("1994-07-22"), "Ожидается список без новых значений");
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
        assertThrows(ConditionsNotMetException.class, () -> inMemoryFilmStorage.create(releaseDateBefore1895Film));
        assertTrue(inMemoryFilmStorage.getFilms().toString().contains("1994-07-22"), "Ожидается список без новых значений");
    }
}

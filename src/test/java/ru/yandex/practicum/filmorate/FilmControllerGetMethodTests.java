package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class FilmControllerGetMethodTests {
    InMemoryFilmStorage inMemoryFilmStorage;
    Film film = Film.builder()
            .name("New film")
            .description("Good new film")
            .releaseDate(LocalDate.of(1994, 07, 22))
            .duration(100)
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryFilmStorage = new InMemoryFilmStorage();
    }

    @Test
    void getFilms_emptyFilmsHashMap_returnsEmpyList() {
        Collection<Film> films = inMemoryFilmStorage.getFilms();
        assertTrue(films.isEmpty(), "Ожидается пустой список");
    }

    @Test
    void getFilms_oneFilmOnHashMap_returnsFilmList() {
        inMemoryFilmStorage.create(film);
        Collection<Film> films = inMemoryFilmStorage.getFilms();
        assertFalse(films.isEmpty(), "Ожидается список с фильмом");
    }

}

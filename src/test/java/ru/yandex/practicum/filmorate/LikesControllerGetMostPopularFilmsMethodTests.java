package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LikesControllerGetMostPopularFilmsMethodTests {
    @Autowired
    UserService userService;
    @Autowired
    InMemoryUserStorage inMemoryUserStorage;
    @Autowired
    InMemoryFilmStorage inMemoryFilmStorage;
    @Autowired
    FilmService filmService;
    User userFirst = User.builder()
            .email("example1@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();
    Film filmFirst = Film.builder()
            .name("New film")
            .description("Good new film")
            .releaseDate(LocalDate.of(1994, 07, 22))
            .duration(100)
            .build();

    Film filmSecond = Film.builder()
            .name("New film2")
            .description("Good new film")
            .releaseDate(LocalDate.of(1994, 07, 22))
            .duration(100)
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        inMemoryFilmStorage.clearStorage();
        inMemoryUserStorage.create(userFirst);
        inMemoryFilmStorage.create(filmFirst);
        inMemoryFilmStorage.create(filmSecond);
        filmService.addLike(1L, 1L);
    }

    @Test
    void getMostPopularFilms_existUsersAndFilm_returnsSuccessStatus() {
        System.out.println(filmService.getMostPopularFilms(1));
        assertTrue(filmService.getMostPopularFilms(1).contains(filmFirst),
                "Ожидается список с фильмом ID-1");
        assertFalse(filmService.getMostPopularFilms(1).contains(filmSecond),
                "В списке не должно быть второго фильма ID-2");
    }
}

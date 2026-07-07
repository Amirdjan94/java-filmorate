package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class LikesControllerAddLikeMethodTests {
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
    Film film = Film.builder()
            .name("New film")
            .description("Good new film")
            .releaseDate(LocalDate.of(1994, 07, 22))
            .duration(100)
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        inMemoryFilmStorage.clearStorage();
        inMemoryUserStorage.create(userFirst);
        inMemoryFilmStorage.create(film);
    }

    @Test
    void addLike_existUsersAndFilm_returnsSuccessStatus() {
        assertTrue(filmService.addLike(1L, 1L).containsValue("Add new like"),
                "Ожидается сообщение об успешном обработке запроса");
        assertTrue(filmService.getFilmById(1L).getLikes().contains(1L),
                "В списке лайков ожидается ID-1");
    }

    @Test
    void addLike_notExistUsersId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class, () -> filmService.addLike(1L, 2L),
                "Ожидается выброс исключения ObjectNotFoundException");
        assertTrue(filmService.getFilmById(1L).getLikes().isEmpty(),
                "Ожидается пустой список лайков");
    }

    @Test
    void addLike_notExistFilmId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class, () -> filmService.addLike(2L, 1L),
                "Ожидается выброс исключения ObjectNotFoundException");
        assertTrue(filmService.getFilmById(1L).getLikes().isEmpty(),
                "Ожидается пустой список лайков");
    }

    @Test
    void addLike_incorrectFilmId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> filmService.addLike(-1L, 1L),
                "Ожидается выброс исключения ConditionsNotMetException");
        assertTrue(filmService.getFilmById(1L).getLikes().isEmpty(),
                "Ожидается пустой список лайков");
    }

    @Test
    void addLike_incorrectUserId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> filmService.addLike(1L, -1L),
                "Ожидается выброс исключения ConditionsNotMetException");
        assertTrue(filmService.getFilmById(1L).getLikes().isEmpty(),
                "Ожидается пустой список лайков");
    }
}

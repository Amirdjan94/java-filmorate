package ru.yandex.practicum.filmorate.db.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class FilmDbDeleteLikeMethodTest {
    private final JdbcTemplate jdbc;
    private final FilmDbStorage filmStorage;
    private Film newFilm;
    private final UserDbStorage userStorage;
    private User newUser;

    private static final String GET_LIKES_COUNT = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";

    @BeforeEach
    void beforeEach() {
        filmStorage.deleteAllFilms();
        userStorage.deleteAllUsers();
        newFilm = filmStorage.create(Film.builder()
                .name("New film")
                .description("Good new film")
                .releaseDate(LocalDate.of(1994, 07, 22))
                .duration(100)
                .mpa(new Mpa())
                .build()
        );
        newUser = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
    }

    @Test
    public void deleteLike_deletLike_successfullDelete() {
        filmStorage.addLike(newFilm, newUser);
        filmStorage.deleteLike(newFilm, newUser);
        Integer count = jdbc.queryForObject(GET_LIKES_COUNT, Integer.class, newFilm.getId(), newUser.getId());
        assertTrue(count == 0);
    }
}

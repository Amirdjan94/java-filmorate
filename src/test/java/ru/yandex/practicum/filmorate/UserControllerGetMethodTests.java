package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserControllerGetMethodTests {
    InMemoryUserStorage inMemoryUserStorage;
    User user = User.builder()
            .email("example@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage = new InMemoryUserStorage();
    }

    @Test
    void getFilms_emptyFilmsHashMap_returnsEmpyList() {
        Collection<User> users = inMemoryUserStorage.getUsers();
        assertTrue(users.isEmpty(), "Ожидается пустой список");
    }

    @Test
    void getFilms_oneFilmOnHashMap_returnsFilmList() {
        inMemoryUserStorage.create(user);
        Collection<User> users = inMemoryUserStorage.getUsers();
        assertFalse(users.isEmpty(), "Ожидается список с пользователями");
    }

}

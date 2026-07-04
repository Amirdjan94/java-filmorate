package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserControllerGetMethodTests {
    UserController userController;
    User user = User.builder()
            .email("example@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
    }

    @Test
    void getFilms_emptyFilmsHashMap_returnsEmpyList() {
        Collection<User> users = userController.getUsers();
        assertTrue(users.isEmpty(), "Ожидается пустой список");
    }

    @Test
    void getFilms_oneFilmOnHashMap_returnsFilmList() {
        userController.create(user);
        Collection<User> users = userController.getUsers();
        assertFalse(users.isEmpty(), "Ожидается список с пользователями");
    }

}

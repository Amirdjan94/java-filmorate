package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerPostMethodTests {
    UserController userController;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
    }

    @Test
    void create_validUser_returnsUser() {
        User validUser = User.builder()
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(userController.create(validUser), validUser);
        assertFalse(userController.getUsers().isEmpty(), "Ожидается НЕ пустой список");
    }

    @Test
    void create_emptyName_returnsUser() {
        User newValidUser = User.builder()
                .login("userLogin")
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("userLogin")
                .build();
        User emptyNameUser = User.builder()
                .login("userLogin")
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("")
                .build();
        assertEquals(userController.create(emptyNameUser), newValidUser);
        assertFalse(userController.getUsers().isEmpty(), "Ожидается НЕ пустой список");
    }
}

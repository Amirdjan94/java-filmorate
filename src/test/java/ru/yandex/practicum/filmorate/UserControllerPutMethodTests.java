package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerPutMethodTests {
    UserController userController;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
        User validUser = User.builder()
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        userController.create(validUser);
    }

    @Test
    void update_validNewEmail_returnsUpdateUser() {
        User validNewEmailUser = User.builder()
                .id(1L)
                .email("exampleNew@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(userController.update(validNewEmailUser), validNewEmailUser);
        assertTrue(userController.getUsers().toString().contains("exampleNew@mail.ru"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_invalidNewEmail_returnsUpdateUser() {
        User invalidNewEmailUser = User.builder()
                .id(1L)
                .email("exampleNew")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.update(invalidNewEmailUser));
        assertTrue(userController.getUsers().toString().contains("example@mail.ru"), "Ожидается список без новых значений");
    }

    @Test
    void update_notExistId_returnsCurrentUser() {
        User updateNotExistIdUser = User.builder()
                .id(2L)
                .email("exampleNew@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.update(updateNotExistIdUser));
        assertFalse(userController.getUsers().toString().contains("exampleNew@mail.ru"), "Ожидается список без новых значений");
    }

    @Test
    void update_validNewLogin_returnsUpdateUser() {
        User validNewLoginUser = User.builder()
                .id(1L)
                .email("example@mail.ru")
                .login("newUserLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(userController.update(validNewLoginUser), validNewLoginUser);
        assertTrue(userController.getUsers().toString().contains("newUserLogin"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_validNewBirthday_returnsUpdateUser() {
        User validNewBirthdayUser = User.builder()
                .id(1L)
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1995, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(userController.update(validNewBirthdayUser), validNewBirthdayUser);
        assertTrue(userController.getUsers().toString().contains("1995-12-27"), "Ожидается список с новыми значениями");
    }

    @Test
    void update_validNewName_returnsUpdateUser() {
        User validNewNameUser = User.builder()
                .id(1L)
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1995, 12, 27))
                .name("NewJhon")
                .build();
        assertEquals(userController.update(validNewNameUser), validNewNameUser);
        assertTrue(userController.getUsers().toString().contains("NewJhon"), "Ожидается список с новыми значениями");
    }
}

package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerPutMethodTests {
    @Autowired
    InMemoryUserStorage inMemoryUserStorage;
    @Autowired
    UserService userService;

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        User validUser = User.builder()
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        inMemoryUserStorage.create(validUser);
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
        assertEquals(userService.update(validNewEmailUser), validNewEmailUser);
        assertTrue(inMemoryUserStorage.getUsers().toString().contains("exampleNew@mail.ru"), "Ожидается список с новыми значениями");
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
        assertThrows(ObjectNotFoundException.class, () -> userService.update(updateNotExistIdUser));
        assertFalse(inMemoryUserStorage.getUsers().toString().contains("exampleNew@mail.ru"), "Ожидается список без новых значений");
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
        assertEquals(userService.update(validNewLoginUser), validNewLoginUser);
        assertTrue(inMemoryUserStorage.getUsers().toString().contains("newUserLogin"), "Ожидается список с новыми значениями");
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
        assertEquals(userService.update(validNewBirthdayUser), validNewBirthdayUser);
        assertTrue(inMemoryUserStorage.getUsers().toString().contains("1995-12-27"), "Ожидается список с новыми значениями");
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
        assertEquals(userService.update(validNewNameUser), validNewNameUser);
        assertTrue(inMemoryUserStorage.getUsers().toString().contains("NewJhon"), "Ожидается список с новыми значениями");
    }
}

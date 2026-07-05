package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerPostMethodTests {
    InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage = new InMemoryUserStorage();
    }

    @Test
    void create_validUser_returnsUser() {
        User validUser = User.builder()
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(inMemoryUserStorage.create(validUser), validUser);
        assertFalse(inMemoryUserStorage.getUsers().isEmpty(), "Ожидается НЕ пустой список");
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
        assertEquals(inMemoryUserStorage.create(emptyNameUser), newValidUser);
        assertFalse(inMemoryUserStorage.getUsers().isEmpty(), "Ожидается НЕ пустой список");
    }
}

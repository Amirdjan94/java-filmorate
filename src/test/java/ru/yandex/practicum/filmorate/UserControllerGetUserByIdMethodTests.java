package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserControllerGetUserByIdMethodTests {
    @Autowired
    InMemoryUserStorage inMemoryUserStorage;
    @Autowired
    UserService userService;
    User user = User.builder()
            .email("example@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        inMemoryUserStorage.create(user);
    }

    @Test
    void getUser_existUserId_returnsUser() {

        User currentUser = userService.getUserById(1L);
        assertTrue(currentUser.equals(user), "Ожидается пользователь с ID-1");
    }

    @Test
    void getUser_notExistUserId_returnObjectNotFoundExceptionr() {
        assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(3L),
                "Ожидается выброс исключения ObjectNotFoundException");
    }

    @Test
    void getUser_incorrectUserId_returnConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> userService.getUserById(-3L),
                "Ожидается выброс исключения ConditionsNotMetException");
    }
}

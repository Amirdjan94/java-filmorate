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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FriendsControllerGetListOfFriendsMethodTests {
    @Autowired
    UserService userService;
    @Autowired
    InMemoryUserStorage inMemoryUserStorage;
    User userFirst = User.builder()
            .email("example1@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();
    User userSecond = User.builder()
            .email("example2@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        inMemoryUserStorage.create(userFirst);
        inMemoryUserStorage.create(userSecond);
        userService.addFriend(1L, 2L);
    }

    @Test
    void getListOfFriends_existUsers_returnsListOfFriends() {
        assertTrue(userService.getListOfFriends(1L).contains(userSecond));
        assertTrue(userService.getListOfFriends(2L).contains(userFirst));
        assertTrue(userService.getUserById(1L).getFriends().contains(2L),
                "Ожидается наличие в списке пользователя с ID-2");
        assertTrue(userService.getUserById(2L).getFriends().contains(1L),
                "Ожидается наличие в списке пользователя с ID-2");
    }

    @Test
    void getListOfFriends_notExistUsersId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class, () -> userService.getListOfFriends(3L),
                "Ожидается выброс исключения ObjectNotFoundException");
    }

    @Test
    void getListOfFriends_incorrectUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> userService.getListOfFriends(-3L),
                "Ожидается выброс исключения ConditionsNotMetException");
    }

}

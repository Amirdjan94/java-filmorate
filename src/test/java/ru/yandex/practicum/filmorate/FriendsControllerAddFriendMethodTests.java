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

@SpringBootTest
public class FriendsControllerAddFriendMethodTests {
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
    }

    @Test
    void addFriends_existUsers_returnsSuccessStatus() {
        assertTrue(userService.addFriend(1L, 2L).containsValue("success"),
                "Ожидается сообщение об успешном обработке запроса");
        assertTrue(userService.getUserById(1L).getFriends().contains(2L),
                "В списке друзей ожидается ID-2");
    }

    @Test
    void addFriends_notExistUsersId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class, () -> userService.addFriend(3L, 2L),
                "Ожидается выброс исключения ObjectNotFoundException");
        assertTrue(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается пустой список друзей");
    }

    @Test
    void addFriends_incorrectUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> userService.addFriend(-3L, 2L),
                "Ожидается выброс исключения ConditionsNotMetException");
        assertTrue(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается пустой список друзей");
    }

    @Test
    void addFriends_sameUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> userService.addFriend(2L, 2L),
                "Ожидается выброс исключения ConditionsNotMetException");
        assertTrue(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается пустой список друзей");
    }
}

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
public class FriendsControllerGetListOfCommonFriendsMethodTests {
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
    User userThird = User.builder()
            .email("example3@mail.ru")
            .login("userLogin")
            .birthday(LocalDate.of(1994, 12, 27))
            .name("Jhon")
            .build();

    @BeforeEach
    void beforeEach() {
        inMemoryUserStorage.clearStorage();
        inMemoryUserStorage.create(userFirst);
        inMemoryUserStorage.create(userSecond);
        inMemoryUserStorage.create(userThird);
        userService.addFriend(1L, 3L);
        userService.addFriend(2L, 3L);
    }

    @Test
    void getListOfCommonFriends_existUsers_returnsCommonFriendsList() {
        assertTrue(userService.getListOfCommonFriends(1L, 2L).contains(userThird),
                "Ожидается наличие в списке пользователя с ID-3");
        assertTrue(userService.getListOfCommonFriends(2L, 1L).contains(userThird),
                "Ожидается наличие в списке пользователя с ID-3");
    }

    @Test
    void getListOfCommonFriends_notExistUsersId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class,
                () -> userService.getListOfCommonFriends(1L, 4L).contains(userThird),
                "Ожидается выброс исключения ObjectNotFoundException");
    }

    @Test
    void getListOfCommonFriends_incorrectUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class,
                () -> userService.getListOfCommonFriends(1L, -2L).contains(userThird),
                "Ожидается выброс исключения ConditionsNotMetException");
    }

    @Test
    void getListOfCommonFriends_sameUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class,
                () -> userService.getListOfCommonFriends(1L, 1L).contains(userThird),
                "Ожидается выброс исключения ConditionsNotMetException");
    }

}

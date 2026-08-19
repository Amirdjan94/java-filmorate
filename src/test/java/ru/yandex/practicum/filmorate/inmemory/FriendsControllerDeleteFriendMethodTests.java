package ru.yandex.practicum.filmorate.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class FriendsControllerDeleteFriendMethodTests {
    @Mock
    private FeedService feedService;
    private UserService userService;
    private InMemoryUserStorage inMemoryUserStorage;
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
        MockitoAnnotations.openMocks(this);
        inMemoryUserStorage = new InMemoryUserStorage();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        doNothing().when(feedService).addFeed(anyLong(), anyLong(), any(), any());
        userService = new UserService(inMemoryUserStorage, filmStorage, feedService);
        inMemoryUserStorage.clearStorage();
        inMemoryUserStorage.create(userFirst);
        inMemoryUserStorage.create(userSecond);
        userService.addFriend(1L, 2L);
    }

    @Test
    void deleteFriend_existUsers_returnsSuccessStatus() {
        assertTrue(userService.deleteFriend(1L, 2L).containsValue("success"));
        assertTrue(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается пустой список друзей");
    }

    @Test
    void deleteFriend_notExistUsersId_returnsObjectNotFoundException() {
        assertThrows(ObjectNotFoundException.class, () -> userService.deleteFriend(3L, 2L),
                "Ожидается выброс исключения ObjectNotFoundException");
        assertFalse(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается НЕ пустой список друзей");
    }

    @Test
    void deleteFriend_incorrectUsersId_returnsConditionsNotMetException() {
        assertThrows(ObjectNotFoundException.class, () -> userService.deleteFriend(-3L, 2L),
                "Ожидается выброс исключения ObjectNotFoundException");
        assertFalse(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается Не пустой список друзей");
    }

    @Test
    void deleteFriend_sameUsersId_returnsConditionsNotMetException() {
        assertThrows(ConditionsNotMetException.class, () -> userService.deleteFriend(2L, 2L),
                "Ожидается выброс исключения ConditionsNotMetException");
        assertFalse(userService.getUserById(1L).getFriends().isEmpty(),
                "Ожидается Не пустой список друзей");
    }
}

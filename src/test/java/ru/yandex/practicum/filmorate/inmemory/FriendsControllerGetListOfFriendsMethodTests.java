package ru.yandex.practicum.filmorate.inmemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
public class FriendsControllerGetListOfFriendsMethodTests {
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
        assertThrows(ObjectNotFoundException.class, () -> userService.getListOfFriends(-3L),
                "Ожидается выброс исключения ObjectNotFoundException");
    }

}

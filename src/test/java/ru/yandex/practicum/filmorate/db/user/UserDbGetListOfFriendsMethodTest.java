package ru.yandex.practicum.filmorate.db.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserDbGetListOfFriendsMethodTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;
    private User user;
    private User friend;

    @BeforeEach
    void beforeEach() {
        userStorage.deleteAllFriends();
        userStorage.deleteAllUsers();
        user = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
        friend = userStorage.create(
                User.builder()
                        .email("secondExample@mail.ru")
                        .login("secondUserLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("newJhon")
                        .build()
        );
    }

    @Test
    public void getListOfFriends_getListOfFollowingConfirmFriends_getOneFriend() {
        userStorage.addFriend(user, friend);
        userStorage.addFriend(friend, user);
        Collection<User> lisOfFriends = userStorage.getListOfFriends(user);
        assertTrue(lisOfFriends.contains(friend));
    }

    @Test
    public void getListOfFriends_getListOfFollowingNotConfirmFriends_getEmptyCollection() {
        userStorage.addFriend(friend, user);
        Collection<User> lisOfFriends = userStorage.getListOfFriends(user);
        assertTrue(lisOfFriends.isEmpty());
    }
}

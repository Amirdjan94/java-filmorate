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
public class UserDbGetListOfCommonFriendsMethodTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;
    private User firstUser;
    private User secondUser;
    private User thirdUser;


    @BeforeEach
    void beforeEach() {
        userStorage.deleteAllFriends();
        userStorage.deleteAllUsers();
        firstUser = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
        secondUser = userStorage.create(
                User.builder()
                        .email("secondExample@mail.ru")
                        .login("secondUserLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("newJhon")
                        .build()
        );
        thirdUser = userStorage.create(
                User.builder()
                        .email("thirdExample@mail.ru")
                        .login("thirdUserLogin")
                        .birthday(LocalDate.of(1994, 12, 29))
                        .name("thirdJhon")
                        .build()
        );
    }

    @Test
    public void getListOfFriends_getListOfCommonFriends_getThirdFriend() {
        userStorage.addFriend(firstUser, thirdUser);
        userStorage.addFriend(secondUser, thirdUser);
        Collection<User> lisOfFriends = userStorage.getListOfCommonFriends(firstUser, secondUser);
        assertTrue(lisOfFriends.contains(thirdUser));
    }

    @Test
    public void getListOfFriends_notConfirmFriends_getThirdFriendEmptyList() {
        userStorage.addFriend(thirdUser, secondUser);
        Collection<User> lisOfFriends = userStorage.getListOfCommonFriends(firstUser, secondUser);
        assertTrue(lisOfFriends.isEmpty());
    }
}

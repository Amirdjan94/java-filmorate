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

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserDbDeleteFriendMethodTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;
    private User user;
    private User friend;

    private static final String SEARCH_FOLLOWED_FRIEND = "SELECT COUNT(*) FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";
    private static final String SEARCH_FOLLOWED_FRIEND_GET_CONFIRM = "SELECT confirmation FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";


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
    public void deleteFriend_deleteFollowingConfirmFriend_deleteLineAndAddNewLine() {
        userStorage.addFriend(user, friend);
        userStorage.addFriend(friend, user);
        userStorage.deleteFriend(user, friend);
        Integer count = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, friend.getId(), user.getId());
        assertTrue(count == 1);
        String confirm = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONFIRM, String.class, friend.getId(), user.getId());
        assertTrue(confirm.equals("FALSE"));
    }

    @Test
    public void deleteFriend_deleteFollowingNotConfirmFriend_deleteLine() {
        userStorage.addFriend(user, friend);
        userStorage.deleteFriend(user, friend);
        Integer count = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, friend.getId(), user.getId());
        assertTrue(count == 0);
    }

    @Test
    public void deleteFriend_deleteFollowedConfirmFriend_ConfirmRowEqualsFalse() {
        userStorage.addFriend(user, friend);
        userStorage.addFriend(friend, user);
        userStorage.deleteFriend(friend, user);
        Integer count = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, user.getId(), friend.getId());
        assertTrue(count == 1);
        String confirm = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONFIRM, String.class, user.getId(), friend.getId());
        assertTrue(confirm.equals("FALSE"));
    }

    @Test
    public void deleteFriend_deleteFollowedNotConfirmFriend_ConfirmRowEqualsFalse() {
        userStorage.addFriend(user, friend);
        userStorage.addFriend(friend, user);
        userStorage.deleteFriend(friend, user);
        Integer count = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, user.getId(), friend.getId());
        assertTrue(count == 1);
        String confirm = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONFIRM, String.class, user.getId(), friend.getId());
        assertTrue(confirm.equals("FALSE"));
    }
}

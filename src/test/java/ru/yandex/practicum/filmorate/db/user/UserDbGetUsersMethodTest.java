package ru.yandex.practicum.filmorate.db.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserDbGetUsersMethodTest {
    private final UserDbStorage userStorage;
    private User newUser;

    @BeforeEach
    void beforeEach() {
        userStorage.deleteAllUsers();
    }

    @Test
    public void getUsers_notEmptyListOfUser_returnsUser() {
        newUser = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
        Collection<User> userOptional = userStorage.getUsers();
        assertTrue(userOptional.contains(newUser));
    }

    @Test
    public void getUsers_emptyListOfUser_returnsUser() {
        Collection<User> userOptional = userStorage.getUsers();
        assertTrue(userOptional.isEmpty());
    }
}

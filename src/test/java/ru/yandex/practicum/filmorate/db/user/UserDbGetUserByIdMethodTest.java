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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserDbGetUserByIdMethodTest {
    private final UserDbStorage userStorage;
    private User user;

    @BeforeEach
    void beforeEach() {
        userStorage.deleteAllUsers();
        user = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin2")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
    }

    @Test
    public void getUser_existUserId_returnsUser() {
        Optional<User> userOptional = userStorage.getUserById(user.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    public void getUser_notExistUserId_returnsEmptyOptional() {
        Optional<User> userOptional = userStorage.getUserById(3L);
        assertThat(userOptional)
                .isEmpty();
    }
}

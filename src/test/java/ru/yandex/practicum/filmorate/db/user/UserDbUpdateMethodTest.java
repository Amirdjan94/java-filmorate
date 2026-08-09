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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class UserDbUpdateMethodTest {
    private final UserDbStorage userStorage;
    private User newUser;
    private User updateUser;

    @BeforeEach
    void beforeEach() {
        userStorage.deleteAllUsers();
        newUser = userStorage.create(
                User.builder()
                        .email("example@mail.ru")
                        .login("userLogin")
                        .birthday(LocalDate.of(1994, 12, 27))
                        .name("Jhon")
                        .build()
        );
    }

    @Test
    public void update_updateEmail_returnsUserWithNewEmail() {
        updateUser = User.builder()
                .id(newUser.getId())
                .email("newExample@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        User updatedUser = userStorage.update(updateUser, newUser);
        assertThat(updatedUser).hasFieldOrPropertyWithValue("email", "newExample@mail.ru");
    }

    @Test
    public void update_updateLogin_returnsUserWithNewLogin() {
        updateUser = User.builder()
                .id(newUser.getId())
                .email("example@mail.ru")
                .login("NewUserLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        User updatedUser = userStorage.update(updateUser, newUser);
        assertThat(updatedUser).hasFieldOrPropertyWithValue("login", "NewUserLogin");
    }

    @Test
    public void update_updateBirthday_returnsUserWithNewBirthday() {
        updateUser = User.builder()
                .id(newUser.getId())
                .email("example@mail.ru")
                .login("NewUserLogin")
                .birthday(LocalDate.of(1994, 12, 28))
                .name("Jhon")
                .build();
        User updatedUser = userStorage.update(updateUser, newUser);
        assertThat(updatedUser).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1994, 12, 28));
    }

    @Test
    public void update_updateName_returnsUserWithNewName() {
        updateUser = User.builder()
                .id(newUser.getId())
                .email("example@mail.ru")
                .login("NewUserLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Amir")
                .build();
        User updatedUser = userStorage.update(updateUser, newUser);
        assertThat(updatedUser).hasFieldOrPropertyWithValue("name", "Amir");
    }
}

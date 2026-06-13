package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerPostMethodTests {
    UserController userController;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
    }

    @Test
    void create_validUser_returnsUser() {
        User validUser = User.builder()
                .email("example@mail.ru")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertEquals(userController.create(validUser), validUser);
        assertFalse(userController.getUsers().isEmpty(), "Ожидается НЕ пустой список");
    }

    @Test
    void create_withoutEmail_returnsExcepthions() {
        User withoutEmailUser = User.builder()
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(withoutEmailUser));
        System.out.println(userController.getUsers());
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_incorrectEmail_returnsExcepthions() {
        User withoutEmailUser = User.builder()
                .email("example")
                .login("userLogin")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(withoutEmailUser));
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_incorrectLoginWithSpace_returnsExcepthions() {
        User incorrectLoginWithSpaceUser = User.builder()
                .email("example@mail.ru")
                .login("user Login")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(incorrectLoginWithSpaceUser));
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_withoutLogin_returnsExcepthions() {
        User withoutLoginUser = User.builder()
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(withoutLoginUser));
        System.out.println(userController.getUsers());
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_loginIsBlanked_returnsExcepthions() {
        User loginIsBlankedUser = User.builder()
                .login("  ")
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(loginIsBlankedUser));
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }

    @Test
    void create_emptyName_returnsUser() {
        User newValidUser = User.builder()
                .login("userLogin")
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("userLogin")
                .build();
        User emptyNameUser = User.builder()
                .login("userLogin")
                .email("example@mail.ru")
                .birthday(LocalDate.of(1994, 12, 27))
                .name("")
                .build();
        assertEquals(userController.create(emptyNameUser), newValidUser);
        assertFalse(userController.getUsers().isEmpty(), "Ожидается НЕ пустой список");
    }

    @Test
    void create_birthdayIsNull_returnsExcepthions() {
        User loginIsNullUser = User.builder()
                .login("userLogin")
                .email("example@mail.ru")
                .birthday(null)
                .name("Jhon")
                .build();
        assertThrows(ConditionsNotMetException.class, () -> userController.create(loginIsNullUser));
        assertTrue(userController.getUsers().isEmpty(), "Ожидается пустой список");
    }
}

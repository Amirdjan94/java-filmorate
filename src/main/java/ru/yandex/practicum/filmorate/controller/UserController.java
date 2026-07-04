package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    public Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получили запрос на пользователей");
        log.info("Передали список пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получили запрос на добавление пользователя");
        log.debug("Входящий запрос - " + user.toString());
        validateAndNormalizeFields(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("В список добавили пользователя - " + user.toString());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получили запрос на обновление информации пользователе");
        log.debug("Входящий запрос - " + user.toString());
        if (user.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("Отсутсвует фильм с указанным Id");
            throw new ConditionsNotMetException("Отсутсвует пользователь с указанным Id");
        }
        normalizeFields(user);
        User currentUser = users.get(user.getId());
        if (user.getEmail() != null) {
            checkEmailCurrentUser(user);
            currentUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            currentUser.setLogin(user.getLogin());
        }
        if (user.getBirthday() != null) {
            currentUser.setBirthday(user.getBirthday());
        }
        if (user.getName() != null || !user.getName().isBlank()) {
            currentUser.setName(user.getName());
        }
        log.info("Информация о пользователе успешно обновлена");
        return currentUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateAndNormalizeFields(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        checkEmailNewUser(user);
        normalizeFields(user);
    }

    private void normalizeFields(User user) {
        if (user.getLogin() != null) {
            user.setLogin(user.getLogin().trim());
        }
        if (user.getName() != null) {
            user.setName(user.getName().trim());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }
    }

    private void checkEmailCurrentUser(User user) {
        for (User value : users.values()) {
            if (user.getId() != value.getId() && user.getEmail().equals(value.getEmail())) {
                log.warn("Имейл уже используется");
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
    }

    private void checkEmailNewUser(User user) {
        for (User value : users.values()) {
            if (user.getEmail().equals(value.getEmail())) {
                log.warn("Имейл уже используется");
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
    }
}


package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.excepton.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    public Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        log.info("Получили запрос на пользователей");
        log.info("Передали список пользователей");
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Получили запрос на добавление пользователя");
        checkEmailNewUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("В список добавили пользователя - " + user.toString());
        return user;
    }

    @Override
    public User update(User user, User currentUser) {
        log.info("Получили запрос на обновление информации пользователе");
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

    @Override
    public Optional<User> getUserById(Long id) {
        log.info("Получили запрос на передачу пользоватля с ID-" + id);
        log.info("Передали пользователя по ID-" + id);
        return Optional.ofNullable(users.get(id));
    }

    public void clearStorage() {
        users.clear();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
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

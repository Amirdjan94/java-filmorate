package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage inMemoryUserStorage) {
        this.userStorage = inMemoryUserStorage;
    }


    public Map<String, String> addFriend(Long userId, Long friendId) { // добавление в друзья
        log.info("Получили запрос на добавление в друзья для пользователя с ID-" + userId + " и ID-" + friendId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(userId, friendId);
        log.debug("Корректные входные данные");
        userStorage.addFriend(getUserById(userId), getUserById(friendId));
        log.info("Добавление в список друзей прошло успешно");
        return Map.of(
                "status", "success",
                "operation", "Add new friend"
        );
    }

    public Map<String, String> deleteFriend(Long userId, Long friendId) { // удаление из друзей
        log.info("Получили запрос на удаление друзей для пользователя с ID-" + userId + " и ID-" + friendId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(userId, friendId);
        log.debug("Корректные входные данные");
        userStorage.deleteFriend(getUserById(userId), getUserById(friendId));
        log.info("Удаление из списка друзей прошло успешно");
        return Map.of(
                "status", "success",
                "operation", "Delete friend"
        );
    }

    public Collection<User> getListOfFriends(Long userId) { // список пользователей, являющихся его друзьями
        log.info("Получили запрос на список друзей для пользователя с ID-" + userId);
        return userStorage.getListOfFriends(getUserById(userId));
    }

    public Collection<User> getListOfCommonFriends(Long firstUserId, Long secondUserId) { // вывод списка общих друзей
        log.info("Получили запрос на список общих друзей для пользователя с ID-" + firstUserId + " и ID-" + secondUserId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(firstUserId, secondUserId);
        log.debug("Корректные входные данные");
        log.info("Передали список общих друзей");
        return userStorage.getListOfCommonFriends(getUserById(firstUserId), getUserById(secondUserId));
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        normalizeFields(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        normalizeFields(user);
        User currentUser = getUserById(user.getId());
        return userStorage.update(user, currentUser);
    }

    public User getUserById(Long id) {
        checkUsersId(id);
        Optional<User> user = userStorage.getUserById(id);
        if (user.isEmpty()) {
            throw new ObjectNotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user.get();
    }

    private void checkDuplicateId(Long firstId, Long secondId) {
        if (firstId == secondId) {
            throw new ConditionsNotMetException("Указан один и тот же пользователь");
        }
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

    private void checkUsersId(Long userId) {
        if (userId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + userId);
        }
    }

    public void deleteUser(long userId) {
        getUserById(userId);
        userStorage.deleteUser(userId);
        log.info("Пользователь с id={} удалён", userId);
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage inMemoryUserStorage;

    public Map<String, String> addFriend(Long userId, Long friendId) { // добавление в друзья
        log.info("Получили запрос на добавление в друзья для пользователя с ID-" + userId + " и ID-" + friendId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(userId, friendId);
        log.debug("Корректные входные данные");
        this.getUserById(userId).getFriends().add(friendId);
        this.getUserById(friendId).getFriends().add(userId);
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
        this.getUserById(userId).getFriends().remove(friendId);
        this.getUserById(friendId).getFriends().remove(userId);
        log.info("Удаление из списка друзей прошло успешно");
        return Map.of(
                "status", "success",
                "operation", "Delete friend"
        );
    }

    public Collection<User> getListOfFriends(Long userId) { // список пользователей, являющихся его друзьями
        log.info("Получили запрос на список друзей для пользователя с ID-" + userId);
        return this.getUserById(userId).getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getListOfCommonFriends(Long firstUserId, Long secondUserId) { // вывод списка общих друзей
        log.info("Получили запрос на список общих друзей для пользователя с ID-" + firstUserId + " и ID-" + secondUserId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(firstUserId, secondUserId);
        log.debug("Корректные входные данные");
        log.info("Передали список общих друзей");
        Set<Long> firstUserFriendsIDs = this.getUserById(firstUserId).getFriends();
        return this.getUserById(secondUserId).getFriends().stream()
                .filter(firstUserFriendsIDs::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getUsers() {
        return inMemoryUserStorage.getUsers();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        normalizeFields(user);
        return inMemoryUserStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        normalizeFields(user);
        User currentUser = getUserById(user.getId());
        return inMemoryUserStorage.update(user, currentUser);
    }

    public User getUserById(Long id) {
        checkUsersId(id);
        Optional<User> user = inMemoryUserStorage.getUserById(id);
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
}

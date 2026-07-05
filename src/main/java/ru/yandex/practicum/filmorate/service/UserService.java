package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Map;
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
        inMemoryUserStorage.getUserById(userId).getFriends().add(friendId);
        inMemoryUserStorage.getUserById(friendId).getFriends().add(userId);
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
        inMemoryUserStorage.getUserById(userId).getFriends().remove(friendId);
        inMemoryUserStorage.getUserById(friendId).getFriends().remove(userId);
        log.info("Удаление из списка друзей прошло успешно");
        return Map.of(
                "status", "success",
                "operation", "Delete friend"
        );
    }

    public Collection<User> getListOfFriends(Long userId) { // список пользователей, являющихся его друзьями
        log.info("Получили запрос на список друзей для пользователя с ID-" + userId);
        return inMemoryUserStorage.getUserById(userId).getFriends().stream()
                .map(inMemoryUserStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getListOfCommonFriends(Long firstUserId, Long secondUserId) { // вывод списка общих друзей
        log.info("Получили запрос на список общих друзей для пользователя с ID-" + firstUserId + " и ID-" + secondUserId);
        log.debug("Запуск валидации входных данных");
        checkDuplicateId(firstUserId, secondUserId);
        log.debug("Корректные входные данные");
        log.info("Передали список общих друзей");
        Set<Long> firstUserFriendsIDs = inMemoryUserStorage.getUserById(firstUserId).getFriends();
        return inMemoryUserStorage.getUserById(secondUserId).getFriends().stream()
                .filter(firstUserFriendsIDs::contains)
                .map(inMemoryUserStorage::getUserById)
                .collect(Collectors.toList());
    }

    private void checkDuplicateId(Long firstId, Long secondId) {
        if (firstId == secondId) {
            throw new ConditionsNotMetException("Указан один и тот же пользователь");
        }
    }
}

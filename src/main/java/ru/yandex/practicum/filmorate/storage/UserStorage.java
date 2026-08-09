package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User create(User user);

    User update(User user, User currentUser);

    Optional<User> getUserById(Long id);

    void addFriend(User user, User friend);

    void deleteFriend(User user, User friend);

    Collection<User> getListOfFriends(User user);

    Collection<User> getListOfCommonFriends(User firstUser, User secondUser);
}

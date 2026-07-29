package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String INSERT_USER = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_ALL_USERS = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, " +
            "birthday = ? WHERE user_id = ?";
    private static final String INSERT_FRIEND = "INSERT INTO follows(following_user_id, followed_user_id)" +
            "VALUES (?, ?)";
    private static final String SEARCH_FRIEND = "SELECT COUNT(*) FROM follows " +
            "WHERE (following_user_id = ? AND followed_user_id = ?) OR" +
            "(following_user_id = ? AND followed_user_id = ?)";
    private static final String CONFIRMATION_ADD_FRIEND = "UPDATE follows SET confirmation = true " +
            "WHERE (following_user_id = ? AND followed_user_id = ?) " +
            "OR (following_user_id = ? AND followed_user_id = ?)";
    private static final String GET_FOLLOWING_FRIENDS = "SELECT users.* FROM users " +
            "JOIN follows ON users.user_id = follows.following_user_id " +
            "WHERE follows.followed_user_id = ? AND follows.confirmation = true";
    private static final String GET_FOLLOWED_FRIENDS = "SELECT users.* FROM users " +
            "JOIN follows ON users.user_id = follows.followed_user_id " +
            "WHERE follows.following_user_id = ?";
    private static final String SEARCH_FOLLOWED_FRIEND = "SELECT COUNT(*) FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";
    private static final String UPDATE_SET_FALSE_CONF = "UPDATE follows SET confirmation = ? WHERE followed_user_id = ?";
    private static final String DELETE_FRIEND = "DELETE FROM follows WHERE (following_user_id = ? AND followed_user_id = ?)";
    private static final String SEARCH_FOLLOWED_FRIEND_GET_CONF = "SELECT confirmation FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_USERS);
    }

    @Override
    public User update(User user, User currentUser) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                currentUser.getId()
        );
        return getUserById(currentUser.getId()).get();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return findOne(FIND_USER_BY_ID, id);
    }

    @Override
    public void addFriend(User user, User friend) {
        Integer count = jdbc.queryForObject(SEARCH_FRIEND, Integer.class, user.getId(),
                friend.getId(), friend.getId(), user.getId());
        if (count == 0) {
            jdbc.update(//TODO: Перенести в baserepos
                    INSERT_FRIEND,
                    user.getId(),
                    friend.getId()
            );
        } else {
            jdbc.update(//TODO: Перенести в baserepos
                    CONFIRMATION_ADD_FRIEND,
                    user.getId(),
                    friend.getId(),
                    friend.getId(),
                    user.getId()
            );
        }
    }

    @Override
    public void deleteFriend(User user, User friend) {
        Integer countFollowedFriend = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, friend.getId(), user.getId());
        Integer countFollowingFriend = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND, Integer.class, user.getId(), friend.getId());
        if (countFollowedFriend != 0) {
            update(UPDATE_SET_FALSE_CONF, false, user.getId());
        } else if (countFollowingFriend != 0) {
            if (jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONF, Boolean.class, user.getId(), friend.getId())) {
                delete(DELETE_FRIEND, user.getId(), friend.getId());
                addFriend(friend, user);
            } else {
                delete(DELETE_FRIEND, user.getId(), friend.getId());
            }
        }
    }

    @Override
    public Collection<User> getListOfFriends(User user) {
        List<User> listFollowingFriend = findMany(GET_FOLLOWING_FRIENDS, user.getId());
        List<User> listFollowedFriend = findMany(GET_FOLLOWED_FRIENDS, user.getId());
        return Stream.concat(listFollowingFriend.stream(), listFollowedFriend.stream())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getListOfCommonFriends(User firstUser, User secondUser) {
        List<User> firstUserFriends = new ArrayList<>(getListOfFriends(firstUser));
        List<User> secondUserFriends = new ArrayList<>(getListOfFriends(secondUser));
        return firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .collect(Collectors.toList());
    }

    public void deleteAllUsers() {
        jdbc.execute("DELETE FROM users");
    }

    public void deleteAllFriends() {
        jdbc.execute("DELETE FROM follows");
    }
}

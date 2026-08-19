package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

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
    private static final String SEARCH_FOLLOWED_FRIEND = "SELECT COUNT(*) FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";
    private static final String UPDATE_SET_FALSE_CONF = "UPDATE follows SET confirmation = ? WHERE followed_user_id = ?";
    private static final String DELETE_FRIEND = "DELETE FROM follows WHERE (following_user_id = ? AND followed_user_id = ?)";
    private static final String SEARCH_FOLLOWED_FRIEND_GET_CONF = "SELECT confirmation FROM follows " +
            "WHERE following_user_id = ? AND followed_user_id = ?";
    private static final String GET_FRIENDS = "SELECT users.* FROM users " +
            "JOIN follows ON users.user_id = follows.following_user_id " +
            "WHERE follows.followed_user_id = ? AND follows.confirmation = true " +
            "UNION " +
            "SELECT users.* FROM users " +
            "JOIN follows ON users.user_id = follows.followed_user_id " +
            "WHERE follows.following_user_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";


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
            Boolean confirmation = jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONF, Boolean.class,
                    user.getId(), friend.getId());
            if (confirmation) {
                jdbc.update(DELETE_FRIEND, user.getId(), friend.getId());
                jdbc.update(INSERT_FRIEND, friend.getId(), user.getId());
            } else {
                jdbc.update(DELETE_FRIEND, user.getId(), friend.getId());
            }

            /*if (jdbc.queryForObject(SEARCH_FOLLOWED_FRIEND_GET_CONF, Boolean.class, user.getId(), friend.getId())) {
                delete(DELETE_FRIEND, user.getId(), friend.getId());
                addFriend(friend, user);
            } else {
                delete(DELETE_FRIEND, user.getId(), friend.getId());
            }*/
        }
    }

    @Override
    public Collection<User> getListOfFriends(User user) {
        return findMany(GET_FRIENDS, user.getId(), user.getId());
    }

    @Override
    public Collection<User> getListOfCommonFriends(User firstUser, User secondUser) {
        return findMany("SELECT u.* " +
                        "FROM users u " +
                        "JOIN follows f1 ON u.user_id = f1.followed_user_id " +
                        "JOIN follows f2 ON u.user_id = f2.followed_user_id " +
                        "WHERE f1.following_user_id = ? " +
                        "  AND f2.following_user_id = ? ",
                firstUser.getId(), secondUser.getId());
    }

    public void deleteAllUsers() {
        jdbc.execute("DELETE FROM users");
    }

    public void deleteAllFriends() {
        jdbc.execute("DELETE FROM follows");
    }

    @Override
    public void deleteUser(long userId) {
        jdbc.update(
                DELETE_USER,
                userId
        );
    }
}

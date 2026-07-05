package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FriendsController {
    private final UserService userService;

    @PutMapping("/{id}/friends/{friendId}")
    public Map<String, String> addFriend(@PathVariable("id") Long userId,
                                         @PathVariable("friendId") Long friendId) {
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Map<String, String> deleteFriend(@PathVariable("id") Long userId,
                                            @PathVariable("friendId") Long friendId) {
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getListOfMutualFriends(@PathVariable("id") Long userId) {
        return userService.getListOfFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getListOfMutualFriends(@PathVariable("id") Long userId,
                                                   @PathVariable("otherId") Long friendId) {
        return userService.getListOfCommonFriends(userId, friendId);
    }
}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserStorage inMemoryUserStorage;

    @GetMapping
    public Collection<User> getUsers() {
        return inMemoryUserStorage.getUsers();
    }

    @GetMapping("/{id}")
    public User getUsersById(@PathVariable("id") Long userId) {
        return inMemoryUserStorage.getUserById(userId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return inMemoryUserStorage.update(user);
    }

}


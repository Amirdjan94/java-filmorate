package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final UserService userService;

    @GetMapping("/users/{id}/feed")
    public Collection<Feed> getListOfMutualFriends(@PathVariable("id") Long userId) {
        userService.getUserById(userId);
        return feedService.getFeedByUserId(userId);
    }
}

package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

public interface FeedStorage {

    Feed create(Feed feed);

    Collection<Feed> getAllFeedByUserId(Long userId);
}

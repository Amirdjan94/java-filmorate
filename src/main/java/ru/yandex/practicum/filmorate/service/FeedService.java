package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.EventOperation;
import ru.yandex.practicum.filmorate.data.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.time.Instant;
import java.util.Collection;

@Service
@Slf4j
public class FeedService {
    @Autowired
    private FeedStorage feedStorage;

    public void createFeed(Feed feed) {
        feedStorage.create(feed);
    }

    public Collection<Feed> getFeedByUserId(Long userId) {
        return feedStorage.getAllFeedByUserId(userId);
    }

    public void addFeed(Long entityId, Long userId, EventType eventType, EventOperation operation) {
        createFeed(Feed.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build());
        log.info("Adding feed: entityId={}, userId={}, eventType={}, operation={}", entityId, userId,
                eventType, operation);
    }
}

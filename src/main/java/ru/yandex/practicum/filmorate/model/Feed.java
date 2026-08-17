package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.data.EventOperation;
import ru.yandex.practicum.filmorate.data.EventType;

@Builder
@Data
public class Feed {
    Long eventId;
    Long timestamp;
    Long userId;
    @Enumerated(EnumType.STRING)
    EventType eventType;
    @Enumerated(EnumType.STRING)
    EventOperation operation;
    Long entityId;

    public Feed() {
    }

    public Feed(Long eventId, Long timestamp, Long userId,
                EventType eventType, EventOperation operation, Long entityId) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }
}
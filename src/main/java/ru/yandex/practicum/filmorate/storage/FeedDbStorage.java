package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

@Repository
@Slf4j
public class FeedDbStorage extends BaseRepository<Feed> implements FeedStorage {

    private static final String INSERT_FEED = "INSERT INTO feed(event_timestamp, user_id, event_type, operation, entity_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_ALL_FEED_BY_USER_ID =
            "SELECT f.*, " +
                    "FROM feed f " +
                    "JOIN users u ON f.user_id = u.user_id " +
                    "WHERE f.user_id = ? ";

    public FeedDbStorage(JdbcTemplate jdbc, RowMapper<Feed> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Feed create(Feed feed) {
        long id = insert(
                INSERT_FEED,
                feed.getTimestamp(),
                feed.getUserId(),
                feed.getEventType().name(),
                feed.getOperation().name(),
                feed.getEntityId()
        );
        feed.setEventId(id);
        return feed;
    }

    @Override
    public Collection<Feed> getAllFeedByUserId(Long userId) {
        return findMany(GET_ALL_FEED_BY_USER_ID, userId);
    }
}

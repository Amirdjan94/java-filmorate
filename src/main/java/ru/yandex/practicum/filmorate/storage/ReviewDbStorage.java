package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {

    private static final String INSERT_REVIEW = "INSERT INTO reviews(film_id, user_id, content, is_positive)" +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_REVIEW_BY_ID = "SELECT reviews.*, " +
            "(SELECT COUNT(*) FROM reviews_likes WHERE review_id = reviews.review_id) - " +
            "(SELECT COUNT(*) FROM reviews_dislikes WHERE review_id = reviews.review_id) AS useful " +
            " FROM reviews " +
            "WHERE review_id = ?";
    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE review_id = ?";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, " +
            "is_positive = ? WHERE review_id = ?";
    private static final String GET_ALL_REVIEW_BY_FILM_ID =
            "SELECT r.*, " +
                    "(SELECT COUNT(*) FROM reviews_likes WHERE review_id = r.review_id) - " +
                    "(SELECT COUNT(*) FROM reviews_dislikes WHERE review_id = r.review_id) AS useful " +
                    "FROM reviews r " +
                    "WHERE r.film_id = ? " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
    private static final String GET_ALL_REVIEW =
            "SELECT r.*, " +
                    "(SELECT COUNT(*) FROM reviews_likes WHERE review_id = r.review_id) - " +
                    "(SELECT COUNT(*) FROM reviews_dislikes WHERE review_id = r.review_id) AS useful " +
                    "FROM reviews r " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
    private static final String INSERT_LIKE_FOR_REVIEW = "INSERT INTO reviews_likes(review_id, user_id) " +
            "VALUES (?, ?)";

    private static final String DELETE_LIKE_FOR_REVIEW = "DELETE FROM reviews_likes WHERE user_id = ? " +
            "AND review_id = ?";

    private static final String INSERT_DISLIKE_FOR_REVIEW = "INSERT INTO reviews_dislikes(review_id, user_id) " +
            "VALUES (?, ?)";

    private static final String DELETE_DISLIKE_FOR_REVIEW = "DELETE FROM reviews_dislikes WHERE user_id = ? " +
            "AND review_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        long id = insert(
                INSERT_REVIEW,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.isPositive()
        );
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review, Review currentReview) {
        update(UPDATE_QUERY,
                review.getContent(),
                review.isPositive(),
                currentReview.getReviewId());
        return getReviewById(currentReview.getReviewId()).get();
    }

    @Override
    public boolean delete(Long reviewId) {
        return delete(DELETE_REVIEW, reviewId);
    }

    @Override
    public Optional<Review> getReviewById(Long reviewId) {
        return findOne(FIND_REVIEW_BY_ID, reviewId);
    }

    @Override
    public Collection<Review> getAllReviewByFilmId(Long filmId, Long count) {
        if (filmId == null) {
            return findMany(GET_ALL_REVIEW, count);
        }
        return findMany(GET_ALL_REVIEW_BY_FILM_ID, filmId, count);
    }

    @Override
    public boolean addLikeForReview(Long reviewId, Long userId) {
        deleteDislikeForReview(reviewId, userId);
        jdbc.update(
                INSERT_LIKE_FOR_REVIEW,
                reviewId,
                userId
        );
        return true;
    }

    @Override
    public boolean deleteLikeForReview(Long reviewId, Long userId) {
        return delete(DELETE_LIKE_FOR_REVIEW,
                userId,
                reviewId);
    }

    @Override
    public boolean addDislikeForReview(Long reviewId, Long userId) {
        deleteLikeForReview(reviewId, userId);
        jdbc.update(
                INSERT_DISLIKE_FOR_REVIEW,
                reviewId,
                userId
        );
        return true;
    }

    @Override
    public boolean deleteDislikeForReview(Long reviewId, Long userId) {
        return delete(DELETE_DISLIKE_FOR_REVIEW,
                userId,
                reviewId);
    }
}

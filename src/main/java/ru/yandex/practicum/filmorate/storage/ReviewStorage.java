package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    boolean update(Review review, Review currentReview);

    boolean delete(Long reviewId);

    Optional<Review> getReviewById(Long reviewId);

    Collection<Review> getAllReviewByFilmId(Long filmId, Long count);

    boolean addLikeForReview(Long reviewId, Long userId);

    public boolean deleteLikeForReview(Long reviewId, Long userId);

    boolean addDislikeForReview(Long reviewId, Long userId);

    public boolean deleteDislikeForReview(Long reviewId, Long userId);
}

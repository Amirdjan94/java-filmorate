package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.EventOperation;
import ru.yandex.practicum.filmorate.data.EventType;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {
    @Autowired
    private ReviewStorage reviewStorage;
    @Autowired
    private UserService userService;
    @Autowired
    private FilmService filmService;
    @Autowired
    private FilmDbStorage filmDbStorage;
    @Autowired
    private UserDbStorage userDbStorage;
    @Autowired
    private FeedService feedService;


    public Review create(Review review) {
        normalizeFields(review);
        checkUserId(review);
        checkFilmId(review);
        Review adderReview = reviewStorage.create(review);
        feedService.addFeed(adderReview.getReviewId(), review.getUserId(), EventType.REVIEW, EventOperation.ADD);
        return adderReview;
    }

    public Review getReviewById(Long id) {
        checkId(id);
        Optional<Review> review = reviewStorage.getReviewById(id);
        if (review.isEmpty()) {
            throw new ObjectNotFoundException("Отзыв с id=" + id + " не найден");
        }
        return review.get();
    }

    public Map<String, String> delete(Long id) {
        Review review = getReviewById(id); // Если нет отзыва по указонному ид, упадет ошибка
        if (reviewStorage.delete(id)) {
            feedService.addFeed(review.getReviewId(), review.getUserId(), EventType.REVIEW, EventOperation.REMOVE);
            return Map.of(
                    "status", "success",
                    "operation", "Delete review"
            );
        } else {
            throw new ConditionsNotMetException("Что-то пошло не так, ни одна запись не удалена");
        }

    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            log.warn("Передан пустой Id");
            throw new ConditionsNotMetException("Id не должен быть пустым");
        }
        normalizeFields(review);
        checkUserId(review);
        checkFilmId(review);
        Review currentReview = getReviewById(review.getReviewId());
        feedService.addFeed(review.getReviewId(), review.getUserId(), EventType.REVIEW, EventOperation.UPDATE);
        return reviewStorage.update(review, currentReview);
    }

    public Collection<Review> getAllReviewByFilmId(Long filmId, Long count) {
        if (filmId != null) {
            checkId(filmId);
            filmDbStorage.getFilmById(filmId);
        }
        return reviewStorage.getAllReviewByFilmId(filmId, count);
    }

    public Map<String, String> addLikeForReview(Long reviewId, Long userId) {
        checkId(reviewId);
        checkId(userId);
        getReviewById(reviewId);
        userService.getUserById(userId);
        if (reviewStorage.addLikeForReview(reviewId, userId)) {
            return Map.of(
                    "status", "success",
                    "operation", "Add like"
            );
        } else {
            throw new ConditionsNotMetException("Что-то пошло не так, не удалось поставить лайк отзыву");
        }
    }

    public Map<String, String> deleteLikeForReview(Long reviewId, Long userId) {
        checkId(reviewId);
        checkId(userId);
        getReviewById(reviewId);
        userService.getUserById(userId);
        if (reviewStorage.deleteLikeForReview(reviewId, userId)) {
            return Map.of(
                    "status", "success",
                    "operation", "Delete like"
            );
        } else {
            throw new ConditionsNotMetException("Что-то пошло не так, не удалось поставить лайк отзыву");
        }
    }

    public Map<String, String> addDislikeForReview(Long reviewId, Long userId) {
        checkId(reviewId);
        checkId(userId);
        getReviewById(reviewId);
        userService.getUserById(userId);
        if (reviewStorage.addDislikeForReview(reviewId, userId)) {
            return Map.of(
                    "status", "success",
                    "operation", "Add dislike"
            );
        } else {
            throw new ConditionsNotMetException("Что-то пошло не так, не удалось поставить лайк отзыву");
        }
    }

    public Map<String, String> deleteDislikeForReview(Long reviewId, Long userId) {
        checkId(reviewId);
        checkId(userId);
        getReviewById(reviewId);
        userService.getUserById(userId);
        if (reviewStorage.deleteDislikeForReview(reviewId, userId)) {
            return Map.of(
                    "status", "success",
                    "operation", "Delete dislike"
            );
        } else {
            throw new ConditionsNotMetException("Что-то пошло не так, не удалось поставить лайк отзыву");
        }
    }

    private void normalizeFields(Review review) {
        review.getContent().trim();
    }

    private void checkId(Long id) {
        if (id <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + id);
        }
    }

    private void checkUserId(Review review) {
        if (review.getUserId() <= 0 || userDbStorage.getUserById(review.getUserId()).isEmpty()) {
            throw new ObjectNotFoundException("Нет пользователя по указанному Id");
        }
    }

    private void checkFilmId(Review review) {
        if (review.getFilmId() <= 0 || filmDbStorage.getFilmById(review.getFilmId()).isEmpty()) {
            throw new ObjectNotFoundException("Нет фильма по указанному Id");
        }
    }
}

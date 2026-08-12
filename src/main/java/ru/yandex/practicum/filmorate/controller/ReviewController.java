package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") Long id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable("id") Long id) {
        return reviewService.delete(id);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    @GetMapping()
    public Collection<Review> getAllReviewByFilmId(@RequestParam(name = "filmId") Long filmId,
                                                   @RequestParam(name = "count", defaultValue = "10") int count) {
        return reviewService.getAllReviewByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Map<String, String> addLikeForReview(@PathVariable("id") Long reviewId,
                                                @PathVariable("userId") Long userId) {
        return reviewService.addLikeForReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Map<String, String> deleteLikeForReview(@PathVariable("id") Long reviewId,
                                                   @PathVariable("userId") Long userId) {
        return reviewService.deleteLikeForReview(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Map<String, String> addDislikeForReview(@PathVariable("id") Long reviewId,
                                                   @PathVariable("userId") Long userId) {
        return reviewService.addDislikeForReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Map<String, String> deleteDislikeForReview(@PathVariable("id") Long reviewId,
                                                      @PathVariable("userId") Long userId) {
        return reviewService.deleteDislikeForReview(reviewId, userId);
    }
}

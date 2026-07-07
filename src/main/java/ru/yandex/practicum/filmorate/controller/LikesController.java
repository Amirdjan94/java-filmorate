package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class LikesController {
    private final FilmService filmService;

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> addLike(@PathVariable("id") Long filmId,
                                       @PathVariable("userId") Long userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> deleteLike(@PathVariable("id") Long filmId,
                                          @PathVariable("userId") Long userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostPopularFilms(@RequestParam(value = "count", defaultValue = "10") int count) {
        return filmService.getMostPopularFilms(count);
    }

}
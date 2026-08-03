package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.service.GenresService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenresController {
    private final GenresService genresService;

    @GetMapping()
    public Collection<Genres> getGenres() {
        return genresService.getGenres();
    }

    @GetMapping("/{id}")
    public Genres getGenresById(@PathVariable("id") Long genresId) {
        return genresService.getGenresById(genresId);
    }

}
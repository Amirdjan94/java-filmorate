package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Genres {
    private Long id;
    private String name;

    public Genres(Long id, String genreName) {
        this.id = id;
        this.name = genreName;
    }
}

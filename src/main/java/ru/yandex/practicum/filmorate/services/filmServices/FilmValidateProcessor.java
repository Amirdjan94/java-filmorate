package ru.yandex.practicum.filmorate.services.filmServices;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmValidateProcessor {
    boolean doValidate(Film film);
}

package ru.yandex.practicum.filmorate.services.filmServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import static ru.yandex.practicum.filmorate.data.Constants.FILM_NAME_MAX_LENGTH;

@Slf4j
public class NameValidator implements FilmValidateProcessor {
    @Override
    public boolean doValidate(Film film) {
        log.debug("Ввалидация поля name");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Пустое название фильма");
            throw new ConditionsNotMetException("Название фильма не может быть пустым");
        }
        if (film.getName().trim().length() > FILM_NAME_MAX_LENGTH) {
            log.warn("Название фильма " + film.getName().trim().length() + " символов");
            throw new ConditionsNotMetException("Название фильма не может быть больше "
                    + FILM_NAME_MAX_LENGTH + " символов");
        }
        log.debug("Поле name валиден");
        return true;
    }
}



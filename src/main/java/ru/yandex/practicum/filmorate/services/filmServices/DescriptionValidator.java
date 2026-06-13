package ru.yandex.practicum.filmorate.services.filmServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import static ru.yandex.practicum.filmorate.data.Constants.FILM_DESCRIPTION_MAX_LENGTH;

@Slf4j
public class DescriptionValidator implements FilmValidateProcessor {
    @Override
    public boolean doValidate(Film film) {
        log.debug("Ввалидация поля description");
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            if (film.getDescription().trim().length() > FILM_DESCRIPTION_MAX_LENGTH) {
                log.warn("Описание фильма " + film.getDescription().trim().length() + " символов");
                throw new ConditionsNotMetException("Описание фильма не может быть больше "
                        + FILM_DESCRIPTION_MAX_LENGTH + "символов");
            }
        }
        log.debug("Поле description валиден");
        return true;
    }
}

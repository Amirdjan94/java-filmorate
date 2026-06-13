package ru.yandex.practicum.filmorate.services.filmServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

import static ru.yandex.practicum.filmorate.data.Constants.FIRST_FILM_RELEASE_DATE;

@Slf4j
public class ReleaseDateValidator implements FilmValidateProcessor {
    @Override
    public boolean doValidate(Film film) {
        log.debug("Ввалидация поля releaseDate");
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FIRST_FILM_RELEASE_DATE)) {
            log.warn("Дата релиза " + film.getReleaseDate());
            throw new ConditionsNotMetException("Дата релиза — не раньше "
                    + FIRST_FILM_RELEASE_DATE);
        }
        log.debug("Поле releaseDate валиден");
        return true;
    }
}

package ru.yandex.practicum.filmorate.services.filmServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
public class DurationValidator implements FilmValidateProcessor {
    @Override
    public boolean doValidate(Film film) {
        log.debug("Ввалидация поля duration");
        if (film.getDuration() < 0) {
            log.warn("Продолжительность фильма меньше 0");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Поле duration валиден");
        return true;
    }
}

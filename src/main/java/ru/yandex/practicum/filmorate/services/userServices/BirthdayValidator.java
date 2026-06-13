package ru.yandex.practicum.filmorate.services.userServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class BirthdayValidator implements UserValidateProcessor {
    @Override
    public boolean doValidate(User user) {
        log.debug("Ввалидация поля birthday");
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения - " + user.getBirthday());
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
        log.debug("Поле birthday валиден");
        return true;
    }
}

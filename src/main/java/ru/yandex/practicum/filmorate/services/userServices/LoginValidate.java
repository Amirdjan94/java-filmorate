package ru.yandex.practicum.filmorate.services.userServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class LoginValidate implements UserValidateProcessor {
    @Override
    public boolean doValidate(User user) {
        log.debug("Ввалидация поля login");
        if (user.getLogin().isBlank() || user.getLogin().trim().contains(" ")) {
            log.warn("Передан пустой login");
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
        log.debug("Поле login валиден");
        return true;
    }
}

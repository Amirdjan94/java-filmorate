package ru.yandex.practicum.filmorate.services.userServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class NullebleFildeValidator implements UserValidateProcessor { // Проверяем поля на null
    @Override
    public boolean doValidate(User user) {
        log.debug("Проверка полей на null");
        if (user.getEmail() == null) {
            log.warn("Передан пустой email");
            throw new ConditionsNotMetException("Email не может быть пустым");
        }
        if (user.getLogin() == null) {
            log.warn("Передан пустой login");
            throw new ConditionsNotMetException("Логин не может быть пустым");
        }
        if (user.getBirthday() == null) {
            log.warn("Передан пустой birthday");
            throw new ConditionsNotMetException("Дата рождения не может быть пустым");
        }
        return true;
    }
}

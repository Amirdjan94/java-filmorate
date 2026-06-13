package ru.yandex.practicum.filmorate.services.userServices;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class CurrentUserEmailValidate implements UserValidateProcessor {
    @Override
    public boolean doValidate(User user) {
        log.debug("Ввалидация поля email");
        if (user.getEmail().isBlank() || !user.getEmail().contains("@") || user.getEmail().trim().contains(" ")) {
            log.warn("Не валидная электронная почта - " + user.getEmail());
            throw new ConditionsNotMetException("Не валидная электронная почта");
        }
        log.debug("Поле birthday валиден");
        return true;
    }
}

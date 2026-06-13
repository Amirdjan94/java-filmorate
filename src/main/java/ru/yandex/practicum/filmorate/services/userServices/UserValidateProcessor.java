package ru.yandex.practicum.filmorate.services.userServices;

import ru.yandex.practicum.filmorate.model.User;

public interface UserValidateProcessor {
    boolean doValidate(User user);
}

package ru.yandex.practicum.filmorate.excepton;

public class ConditionsNotMetException extends RuntimeException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}

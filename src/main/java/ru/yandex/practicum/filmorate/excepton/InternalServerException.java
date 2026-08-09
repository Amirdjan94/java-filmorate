package ru.yandex.practicum.filmorate.excepton;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}

package ru.yandex.practicum.filmorate.excepton;

public class ErrorResponse {
    private final String error;
    private final String description;

    public ErrorResponse(String description, String error) {
        this.description = description;
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public String getError() {
        return error;
    }
}

package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Director {
    private Long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
    private String lastname;

    public Director(Long id, String name, String lastname) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
    }
}
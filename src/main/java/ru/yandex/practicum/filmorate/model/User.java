package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Long id;
    @Email
    private String email;
    private String login;
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past
    private LocalDate birthday;
}

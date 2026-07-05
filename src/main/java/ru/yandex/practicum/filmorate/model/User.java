package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Long id;
    @Email
    @NotBlank
    @NotNull
    private String email;
    @NotBlank
    @NotNull
    private String login;
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past
    @NotNull
    private LocalDate birthday;
    @EqualsAndHashCode.Exclude
    private Set<Long> friends;

    @Builder
    public User(Long id, String email, String login, String name,
                LocalDate birthday, Set<Long> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends != null ? friends : new HashSet<>();
    }
}

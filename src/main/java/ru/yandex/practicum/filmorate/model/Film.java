package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film..
 */
@Data
@Builder
public class Film {
    @EqualsAndHashCode.Exclude
    private Long id;
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 200)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Min(1)
    private Integer duration;
    @EqualsAndHashCode.Exclude
    private Set<Long> likes;

    @Builder
    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration,
                Set<Long> likes) {
        this.description = description;
        this.duration = duration;
        this.id = id;
        this.likes = likes != null ? likes : new HashSet<>();
        this.name = name;
        this.releaseDate = releaseDate;
    }
}

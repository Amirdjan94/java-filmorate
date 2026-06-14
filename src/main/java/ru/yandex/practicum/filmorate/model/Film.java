package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
}

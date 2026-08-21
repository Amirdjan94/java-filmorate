package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {
    private Long id;
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 200)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Min(1)
    private Integer duration;
    @EqualsAndHashCode.Exclude
    private Set<Long> likes;
    private Mpa mpa;

    private Set<Genres> genres;
    private Set<Director> directors;


    @Builder
    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration,
                Set<Long> likes, Mpa mpa, Set<Genres> genres, Set<Director> directors) {
        this.description = description;
        this.duration = duration;
        this.id = id;
        this.likes = likes != null ? likes : new HashSet<>();
        this.name = name;
        this.releaseDate = releaseDate;
        this.mpa = mpa;
        this.genres = genres != null ? genres : new HashSet<Genres>();
        this.directors = directors != null ? directors : new HashSet<Director>();
    }

    public Film() {
        this.genres = new HashSet<Genres>();
    }
}

package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {
    Long reviewId;
    @NotBlank
    @Size(max = 200)
    String content;
    @NotNull
    Long userId;
    @NotNull
    Long filmId;
    Long useful = 0L;
    @NotNull
    @JsonProperty("isPositive")
    Boolean isPositive;

    @JsonProperty("isPositive")
    public boolean isPositive() {
        return isPositive;
    }

    @JsonIgnore
    public boolean getPositive() {
        return isPositive;
    }

}
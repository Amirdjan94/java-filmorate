package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class MpaDto {
    Long id;
    String name;
}

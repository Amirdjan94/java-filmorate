package ru.yandex.practicum.filmorate.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
@NoArgsConstructor
public class MpaMapper {

    public static MpaDto mapToMpaDto(Mpa mpa) {
        MpaDto mpaDto = new MpaDto();
        mpaDto.setId(mpa.getRatingMpaId());
        mpaDto.setName(mpa.getRatingMPAname());
        return mpaDto;
    }
}
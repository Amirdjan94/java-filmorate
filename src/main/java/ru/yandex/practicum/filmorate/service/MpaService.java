package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    public Collection<MpaDto> getMpaList() {
        return mpaDbStorage.getMpaList().stream().map(MpaMapper::mapToMpaDto).collect(Collectors.toList());
    }

    public MpaDto getMpaById(Long id) {
        checkMpaId(id);
        Optional<Mpa> mpa = mpaDbStorage.getMpaById(id);
        if (mpa.isEmpty()) {
            throw new ObjectNotFoundException("Рейтинг с id=" + id + " не найден");
        }
        return MpaMapper.mapToMpaDto(mpa.get());
    }

    private void checkMpaId(Long filmId) {
        if (filmId <= 0L) {
            throw new ConditionsNotMetException("Не корректный ID - " + filmId);
        }
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("ID должен быть указан");
        }
        return directorStorage.findById(id).orElseThrow(() -> new ObjectNotFoundException("Режиссер " + id +
                "с таким ID не найден!"));
    }

    public Director create(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ConditionsNotMetException("Имя режиссера должно быть указано!");
        }

        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director == null) {
            throw new ConditionsNotMetException("Режиссер не может быть null");
        }
        if (director.getId() == null) {
            throw new ConditionsNotMetException("ID должен быть указан");
        }
        Director newDirector = findById(director.getId());
        if (director.getName() != null && !director.getName().isBlank()) {
            newDirector.setName(director.getName());
        }
        if (director.getLastname() != null && !director.getLastname().isBlank()) {
            newDirector.setLastname(director.getLastname());
        }
        return directorStorage.update(newDirector);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("ID должен быть указан");
        }
        findById(id);
        directorStorage.delete(id);
    }
}


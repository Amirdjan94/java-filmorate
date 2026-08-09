package ru.yandex.practicum.filmorate.db.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class MpaDbGetMpaByIdMethodTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void getMpaList_existUserId_returnsMpa() {
        Optional<Mpa> mpa = mpaDbStorage.getMpaById(1L);
        assertTrue(mpa.isPresent());
    }

    @Test
    public void getMpaList_notExistUserId_returnsMpa() {
        Optional<Mpa> mpa = mpaDbStorage.getMpaById(1000L);
        assertTrue(mpa.isEmpty());
    }
}

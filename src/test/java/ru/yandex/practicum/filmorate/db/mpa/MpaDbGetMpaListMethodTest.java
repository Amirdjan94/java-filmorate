package ru.yandex.practicum.filmorate.db.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class MpaDbGetMpaListMethodTest {
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void getMpaList_returnsMpaList() {
        Collection<Mpa> mpaList = mpaDbStorage.getMpaList();
        assertTrue(!mpaList.isEmpty());
        assertTrue(mpaList.contains(mpaDbStorage.getMpaById(1L).get()));
    }
}

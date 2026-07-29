//package ru.yandex.practicum.filmorate.inmemory;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import ru.yandex.practicum.filmorate.excepton.ConditionsNotMetException;
//import ru.yandex.practicum.filmorate.excepton.ObjectNotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.service.FilmService;
//import ru.yandex.practicum.filmorate.service.UserService;
//import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
//import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//@SpringBootTest
//class FilmControllerGetFilmByIdMethodTests {
//    InMemoryFilmStorage inMemoryFilmStorage;
//    InMemoryUserStorage inMemoryUserStorage;
//    FilmService filmService;
//    UserService userService;
//    Film film = Film.builder()
//            .name("New film")
//            .description("Good new film")
//            .releaseDate(LocalDate.of(1994, 07, 22))
//            .duration(100)
//            .build();
//
//    @BeforeEach
//    void beforeEach() {
//        inMemoryFilmStorage = new InMemoryFilmStorage();
//        inMemoryUserStorage = new InMemoryUserStorage();
//        userService = new UserService(inMemoryUserStorage);
//        filmService = new FilmService(inMemoryFilmStorage, userService);
//        inMemoryFilmStorage.clearStorage();
//        filmService.create(film);
//    }
//
//    @Test
//    void getFilm_existFilmId_returnsFilm() {
//
//        Film currentFilm = filmService.getFilmById(1L);
//        assertTrue(film.equals(currentFilm), "Ожидается фильм");
//    }
//
//    @Test
//    void getFilm_notExistFilmId_returnsObjectNotFoundException() {
//        assertThrows(ObjectNotFoundException.class, () -> filmService.getFilmById(3L),
//                "Ожидается выброс исключения ObjectNotFoundException");
//    }
//
//    @Test
//    void getFilm_incorrectFilmId_returnsConditionsNotMetException() {
//        assertThrows(ConditionsNotMetException.class, () -> filmService.getFilmById(-3L),
//                "Ожидается выброс исключения ConditionsNotMetException");
//    }
//
//}

package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;
    private InMemoryUserStorage userStorage;
    private MpaStorage mpaStorage;
    private GenreStorage genreStorage;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();

        mpaStorage = new MpaStorage() {
            @Override
            public List<MpaRating> getAllMpaRatings() {
                return List.of(
                        new MpaRating(1, "G"),
                        new MpaRating(2, "PG"),
                        new MpaRating(3, "PG-13"),
                        new MpaRating(4, "R"),
                        new MpaRating(5, "NC-17")
                );
            }

            @Override
            public MpaRating getMpaRatingById(int id) {
                return getAllMpaRatings().stream()
                        .filter(rating -> rating.getId() == id)
                        .findFirst()
                        .orElse(null);
            }
        };

        genreStorage = new GenreStorage() {
            @Override
            public List<Genre> getAllGenres() {
                return List.of(
                        new Genre(1, "Комедия"),
                        new Genre(2, "Драма"),
                        new Genre(3, "Мультфильм"),
                        new Genre(4, "Триллер"),
                        new Genre(5, "Документальный"),
                        new Genre(6, "Боевик")
                );
            }

            @Override
            public Genre getGenreById(int id) {
                return getAllGenres().stream()
                        .filter(genre -> genre.getId() == id)
                        .findFirst()
                        .orElse(null);
            }
        };

        FilmService filmService = new FilmService(
                filmStorage,
                userStorage,
                mpaStorage,
                genreStorage
        );

        controller = new FilmController(filmService);
    }

    @Test
    void addFilm_validFilm_shouldCreateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);

        Film result = controller.addFilm(film);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Интерстеллар", result.getName());
    }

    @Test
    void addFilm_nullName_shouldThrowException() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_emptyName_shouldThrowException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_descriptionMoreThan200Characters_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_descriptionExactly200Characters_shouldCreateFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(200));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film result = controller.addFilm(film);

        assertNotNull(result);
        assertEquals(200, result.getDescription().length());
    }

    @Test
    void addFilm_releaseDateBeforeMinimum_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_releaseDateExactlyMinimum_shouldCreateFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        Film result = controller.addFilm(film);

        assertNotNull(result);
        assertEquals(LocalDate.of(1895, 12, 28), result.getReleaseDate());
    }

    @Test
    void addFilm_zeroDuration_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_negativeDuration_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Фильм");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_emptyFilm_shouldThrowException() {
        Film film = new Film();

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void addFilm_existingMpa_shouldCreateFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpaRating(new MpaRating(1, "G"));

        Film result = controller.addFilm(film);

        assertNotNull(result);
        assertEquals(1, result.getMpaRating().getId());
    }

    @Test
    void addFilm_nonExistingMpa_shouldThrowNotFoundException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpaRating(new MpaRating(10, "Unknown"));

        assertThrows(
                NotFoundException.class,
                () -> controller.addFilm(film)
        );
    }

    @Test
    void updateFilm_existingFilm_shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Старое название");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Новое название");
        updatedFilm.setDescription("Новое описание");
        updatedFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updatedFilm.setDuration(120);

        Film result = controller.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Новое название", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(LocalDate.of(2021, 1, 1), result.getReleaseDate());
        assertEquals(120, result.getDuration());
    }

    @Test
    void updateFilm_zeroId_shouldThrowException() {
        Film film = new Film();

        assertThrows(ValidationException.class, () -> controller.updateFilm(film));
    }

    @Test
    void updateFilm_nonExistingFilm_shouldThrowNotFoundException() {
        Film film = new Film();
        film.setId(999);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> controller.updateFilm(film)
        );

        assertEquals("Фильм не найден", exception.getMessage());
    }

    @Test
    void updateFilm_emptyName_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("");

        assertThrows(ValidationException.class, () -> controller.updateFilm(updatedFilm));
    }

    @Test
    void updateFilm_descriptionMoreThan200Characters_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setDescription("А".repeat(201));

        assertThrows(ValidationException.class, () -> controller.updateFilm(updatedFilm));
    }

    @Test
    void updateFilm_releaseDateBeforeMinimum_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.updateFilm(updatedFilm));
    }

    @Test
    void updateFilm_negativeDuration_shouldThrowException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setDuration(-1);

        assertThrows(ValidationException.class, () -> controller.updateFilm(updatedFilm));
    }

    @Test
    void getFilms_shouldReturnAllFilms() {
        Film firstFilm = new Film();
        firstFilm.setName("Первый фильм");
        firstFilm.setDescription("Описание");
        firstFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        firstFilm.setDuration(100);

        Film secondFilm = new Film();
        secondFilm.setName("Второй фильм");
        secondFilm.setDescription("Описание");
        secondFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        secondFilm.setDuration(120);

        controller.addFilm(firstFilm);
        controller.addFilm(secondFilm);

        Collection<Film> result = controller.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(firstFilm));
        assertTrue(result.contains(secondFilm));
    }

    @Test
    void findById_existingFilm_shouldReturnFilm() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        Film result = controller.findById(createdFilm.getId());

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Фильм", result.getName());
    }

    @Test
    void findById_nonExistingFilm_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> controller.findById(999)
        );

        assertEquals("Фильм не найден.", exception.getMessage());
    }

    @Test
    void addLike_existingFilmAndUser_shouldAddLike() {
        Film film = createFilm("Фильм");
        Film createdFilm = controller.addFilm(film);

        User user = createUser("test");

        Film result = controller.addLike(createdFilm.getId(), user.getId());

        assertNotNull(result.getLikes());
        assertTrue(result.getLikes().contains(user.getId()));
    }

    @Test
    void addLike_nonExistingUser_shouldThrowNotFoundException() {
        Film film = createFilm("Фильм");
        Film createdFilm = controller.addFilm(film);

        assertThrows(
                NotFoundException.class,
                () -> controller.addLike(createdFilm.getId(), 999)
        );
    }

    @Test
    void addLike_nonExistingFilm_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> controller.addLike(999, 1)
        );
    }

    @Test
    void removeLike_existingFilmAndUser_shouldRemoveLike() {
        Film film = createFilm("Фильм");
        Film createdFilm = controller.addFilm(film);

        User user = createUser("test");

        controller.addLike(createdFilm.getId(), user.getId());

        Film result = controller.deleteLike(createdFilm.getId(), user.getId());

        assertNotNull(result.getLikes());
        assertFalse(result.getLikes().contains(user.getId()));
    }

    @Test
    void getTopFilms_shouldReturnFilmsSortedByLikes() {
        Film firstFilm = controller.addFilm(createFilm("Первый"));
        Film secondFilm = controller.addFilm(createFilm("Второй"));
        Film thirdFilm = controller.addFilm(createFilm("Третий"));

        User firstUser = createUser("first");
        User secondUser = createUser("second");
        User thirdUser = createUser("third");

        controller.addLike(firstFilm.getId(), firstUser.getId());

        controller.addLike(secondFilm.getId(), firstUser.getId());
        controller.addLike(secondFilm.getId(), secondUser.getId());

        controller.addLike(thirdFilm.getId(), firstUser.getId());
        controller.addLike(thirdFilm.getId(), secondUser.getId());
        controller.addLike(thirdFilm.getId(), thirdUser.getId());

        Collection<Film> result = controller.findTop10(2);

        assertEquals(2, result.size());
        assertEquals("Третий", result.iterator().next().getName());
    }

    @Test
    void updateFilm_invalidDescription_shouldNotChangeExistingFilm() {
        Film film = new Film();
        film.setName("Старое название");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        Film createdFilm = controller.addFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Новое название");
        updatedFilm.setDescription("А".repeat(201));

        assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(updatedFilm)
        );

        Film result = controller.findById(createdFilm.getId());

        assertEquals("Старое название", result.getName());
        assertEquals("Старое описание", result.getDescription());
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        return film;
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1995, 5, 10));
        userStorage.addUser(user);
        return user;
    }
}
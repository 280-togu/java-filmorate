package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 2");
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@example.com", "user1", "User One",
                LocalDate.of(1990, 1, 10)
        );

        jdbcTemplate.update(
                "INSERT INTO films (id, name, description, release_date, duration, mpa_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                1, "Test Film", "Test Description",
                LocalDate.of(2020, 5, 10), 100, 1
        );

        jdbcTemplate.update(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                1, 1
        );

        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                1, 1
        );
    }

    @Test
    void shouldReturnFilmById() {
        Film film = filmStorage.getFilmById(1);

        assertNotNull(film);
        assertEquals(1, film.getId());
        assertEquals("Test Film", film.getName());
        assertEquals("Test Description", film.getDescription());
        assertEquals(LocalDate.of(2020, 5, 10), film.getReleaseDate());
        assertEquals(100, film.getDuration());
        assertEquals(MpaRating.G, film.getMpaRating());
        assertEquals(Set.of(Genre.COMEDY), film.getGenres());
        assertEquals(Set.of(1), film.getLikes());
    }

    @Test
    void shouldReturnAllFilms() {
        List<Film> films = filmStorage.getAllFilms();

        assertEquals(1, films.size());
        assertEquals("Test Film", films.get(0).getName());
    }

    @Test
    void shouldAddFilm() {
        Film film = new Film();
        film.setName("New Film");
        film.setDescription("New Description");
        film.setReleaseDate(LocalDate.of(2022, 6, 15));
        film.setDuration(120);
        film.setMpaRating(MpaRating.PG);
        film.setGenres(Set.of(Genre.DRAMA, Genre.THRILLER));
        film.setLikes(Set.of(1));

        filmStorage.addFilm(film);

        assertTrue(film.getId() > 0);

        Film savedFilm = filmStorage.getFilmById(film.getId());

        assertNotNull(savedFilm);
        assertEquals("New Film", savedFilm.getName());
        assertEquals("New Description", savedFilm.getDescription());
        assertEquals(LocalDate.of(2022, 6, 15), savedFilm.getReleaseDate());
        assertEquals(120, savedFilm.getDuration());
        assertEquals(MpaRating.PG, savedFilm.getMpaRating());
        assertEquals(Set.of(Genre.DRAMA, Genre.THRILLER), savedFilm.getGenres());
        assertEquals(Set.of(1), savedFilm.getLikes());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.getFilmById(1);
        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setReleaseDate(LocalDate.of(2021, 8, 20));
        film.setDuration(110);
        film.setMpaRating(MpaRating.R);
        film.setGenres(Set.of(Genre.ACTION, Genre.THRILLER));
        film.setLikes(Set.of());

        filmStorage.updateFilm(film);

        Film updatedFilm = filmStorage.getFilmById(1);

        assertNotNull(updatedFilm);
        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals("Updated Description", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2021, 8, 20), updatedFilm.getReleaseDate());
        assertEquals(110, updatedFilm.getDuration());
        assertEquals(MpaRating.R, updatedFilm.getMpaRating());
        assertEquals(Set.of(Genre.ACTION, Genre.THRILLER), updatedFilm.getGenres());
        assertTrue(updatedFilm.getLikes().isEmpty());
    }

    @Test
    void shouldDeleteFilm() {
        filmStorage.deleteFilm(1);

        assertNull(filmStorage.getFilmById(1));
        assertTrue(filmStorage.getAllFilms().isEmpty());

        Integer genresCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_genres WHERE film_id = ?",
                Integer.class, 1
        );
        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?",
                Integer.class, 1
        );

        assertEquals(0, genresCount);
        assertEquals(0, likesCount);
    }
}
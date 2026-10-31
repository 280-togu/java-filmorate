package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getAllGenresShouldReturnAllGenresFromDatabase() {
        Map<Integer, String> expected = jdbcTemplate.query(
                "SELECT id, name FROM genres",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(Genre::getId, Genre::getName));

        Map<Integer, String> actual = genreDbStorage.getAllGenres().stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).isNotEmpty();
    }

    @Test
    void getGenreByIdShouldReturnGenreWhenItExists() {
        Genre expected = jdbcTemplate.queryForObject(
                "SELECT id, name FROM genres ORDER BY id LIMIT 1",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"))
        );

        Genre actual = genreDbStorage.getGenreById(expected.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    void getGenreByIdShouldReturnNullWhenGenreDoesNotExist() {
        Genre actual = genreDbStorage.getGenreById(Integer.MIN_VALUE);

        assertThat(actual).isNull();
    }
}
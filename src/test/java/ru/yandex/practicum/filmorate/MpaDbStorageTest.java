package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getAllMpaRatingsShouldReturnAllRatingsFromDatabase() {
        Map<Integer, String> expected = jdbcTemplate.query(
                "SELECT id, name FROM mpa_ratings",
                (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(MpaRating::getId, MpaRating::getName));

        Map<Integer, String> actual = mpaDbStorage.getAllMpaRatings().stream()
                .collect(Collectors.toMap(MpaRating::getId, MpaRating::getName));

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).isNotEmpty();
    }

    @Test
    void getMpaRatingByIdShouldReturnRatingWhenItExists() {
        MpaRating expected = jdbcTemplate.queryForObject(
                "SELECT id, name FROM mpa_ratings ORDER BY id LIMIT 1",
                (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name"))
        );

        MpaRating actual = mpaDbStorage.getMpaRatingById(expected.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    @Test
    void getMpaRatingByIdShouldReturnNullWhenRatingDoesNotExist() {
        MpaRating actual = mpaDbStorage.getMpaRatingById(Integer.MIN_VALUE);

        assertThat(actual).isNull();
    }
}
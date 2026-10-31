
package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";

        return jdbcTemplate.query(
                sql, (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public MpaRating getMpaRatingById(int id) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name")),
                id
        ).stream().findFirst().orElse(null);
    }
}

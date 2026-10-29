
package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Film film = new Film();
                film.setId(rs.getInt("id"));
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                java.sql.Date releaseDate = rs.getDate("release_date");
                film.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate() : null);
                film.setDuration(rs.getInt("duration"));
                int mpaId = rs.getInt("mpa_id");
                if (!rs.wasNull()) {
                    film.setMpaRating(MpaRating.fromId(mpaId));
                }
                film.setGenres(getGenresByFilmId(film.getId()));
                film.setLikes(getLikesByFilmId(film.getId()));
                return film;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            java.sql.Date releaseDate = rs.getDate("release_date");
            film.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate() : null);
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                film.setMpaRating(MpaRating.fromId(mpaId));
            }
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setLikes(getLikesByFilmId(film.getId()));
            return film;
        });
    }

    @Override
    public void addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? java.sql.Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            if (film.getMpaRating() != null) {
                ps.setInt(5, film.getMpaRating().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.intValue());
        }
        String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(genreSql, film.getId(), genre.getId());
        }
        String likesSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        for (Integer userId : film.getLikes()) {
            jdbcTemplate.update(likesSql, film.getId(), userId);
        }
    }

    @Override
    public void updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? java.sql.Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                film.getId());
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());
        String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(genreSql, film.getId(), genre.getId());
        }
        String deleteLikesSql = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteLikesSql, film.getId());
        String likesSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        for (Integer userId : film.getLikes()) {
            jdbcTemplate.update(likesSql, film.getId(), userId);
        }
    }

    @Override
    public void deleteFilm(int id) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                Genre.fromId(rs.getInt("genre_id")), filmId));
    }

    private Set<Integer> getLikesByFilmId(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getInt("user_id"), filmId));
    }
}

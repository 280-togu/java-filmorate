package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Не удалось добавить фильм: название не может быть пустым.");
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Не удалось добавить фильм: число символов больше 200.");
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Не удалось добавить фильм: дата релиза раньше 28.12.1895.");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() <= 0) {
            log.warn("Не удалось добавить фильм: продолжительность должна быть положительной.");
            throw new ValidationException("Продолжительность должна быть положительной.");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} с названием '{}' добавлен", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == 0) {
            log.warn("Не обновить фильм: Id должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            log.warn("Не обновить фильм: фильм не найден.");
            throw new ValidationException("Фильм не найден");
        }
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.warn("Не удалось обновить фильм: название не может быть пустым.");
                throw new ValidationException("Название не может быть пустым.");
            }
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() > 200) {
                log.warn("Не удалось обновить фильм: максимальная длина описания - 200 символов.");
                throw new ValidationException("Максимальная длина описания - 200 символов.");
            }
            oldFilm.setDescription(newFilm.getDescription());
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isBefore(minReleaseDate)) {
                log.warn("Не удалось обновить фильм: дата релиза должна быть не раньше 28.12.1895");
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
            }
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != 0) {
            if (newFilm.getDuration() <= 0) {
                log.warn("Не удалось обновить фильм: продолжительность должна быть положительной.");
                throw new ValidationException("Продолжительность должна быть положительной.");
            }
            oldFilm.setDuration(newFilm.getDuration());
        }
        log.info("Фильм под ID {} - успешно обновлён", oldFilm.getId());
        return oldFilm;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;


@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    public FilmService(final FilmStorage filmStorage, final UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Film create(Film film) {
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
        filmStorage.addFilm(film);
        log.info("Фильм {} с названием '{}' добавлен", film.getId(), film.getName());
        return film;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == 0) {
            log.warn("Не обновить фильм: Id должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        Film oldFilm = filmStorage.getFilmById(newFilm.getId());

        if (oldFilm == null) {
            log.warn("Не обновить фильм: фильм не найден.");
            throw new NotFoundException("Фильм не найден");
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
        filmStorage.updateFilm(oldFilm);
        return oldFilm;
    }

    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        if (user == null) {
            throw new NotFoundException("Пользователя не существует.");
        }
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователя не существует.");
        }
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        return film;
    }

    public Collection<Film> getTopFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше нуля.");
        }
        return filmStorage.getAllFilms().stream().sorted((p1, p2) -> Integer.compare(p2.getLikes().size(), p1.getLikes().size())).limit(count).toList();
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм не найден.");
        }
        return film;
    }

    private int getNextId() {
        int currentMaxId = filmStorage.getAllFilms()
                .stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

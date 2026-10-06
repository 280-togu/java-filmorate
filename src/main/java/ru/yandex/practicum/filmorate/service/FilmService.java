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
import java.util.Comparator;


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
        if (newFilm.getName() != null && newFilm.getName().isBlank()) {
            log.warn("Не удалось обновить фильм: название не может быть пустым.");
            throw new ValidationException("Название не может быть пустым.");
        }

        if (newFilm.getDescription() != null && newFilm.getDescription().length() > 200) {
            log.warn("Не удалось обновить фильм: максимальная длина описания - 200 символов.");
            throw new ValidationException("Максимальная длина описания - 200 символов.");
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

        if (newFilm.getReleaseDate() != null
                && newFilm.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Не удалось обновить фильм: дата релиза должна быть не раньше 28.12.1895");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (newFilm.getDuration() != 0 && newFilm.getDuration() <= 0) {
            log.warn("Не удалось обновить фильм: продолжительность должна быть положительной.");
            throw new ValidationException("Продолжительность должна быть положительной.");
        }
        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != 0) {
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
            log.warn("Не удалось поставить лайк: пользователь с ID {} не найден.", userId);
            throw new NotFoundException("Пользователя не существует.");
        }
        if (film == null) {
            log.warn("Не удалось поставить лайк: фильм с ID {} не найден.", filmId);
            throw new NotFoundException("Фильм не найден.");
        }
        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (user == null) {
            log.warn("Не удалось убрать лайк: пользователь с ID {} не найден.", userId);
            throw new NotFoundException("Пользователя не существует.");
        }
        if (film == null) {
            log.warn("Не удалось убрать лайк: фильм с ID {} не найден.", filmId);
            throw new NotFoundException("Фильм не найден.");
        }
        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь с ID {} убрал лайк фильму с ID {}", userId, filmId);
        return film;
    }

    public Collection<Film> getTopFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше нуля.");
        }
        log.info("Запрошены топ {} фильмов", count);
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    public Film getFilmById(Integer id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            log.warn("Фильм с ID {} не найден.", id);
            throw new NotFoundException("Фильм не найден.");
        }
        return film;
    }
}

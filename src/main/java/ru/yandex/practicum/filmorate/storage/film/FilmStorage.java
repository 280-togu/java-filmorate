package ru.yandex.practicum.filmorate.storage.film;


import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film getFilmById(int id);

    List<Film> getAllFilms();

    void addFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getTopFilms(int count);
}

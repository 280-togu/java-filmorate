package ru.yandex.practicum.filmorate.storage.film;


import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film getFilmById(int id);

    List<Film> getAllFilms();

    void addFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(int id);
}

package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum Genre {
    COMEDY(1),
    DRAMA(2),
    CARTOON(3),
    THRILLER(4),
    DOCUMENTARY(5),
    ACTION(6);

    private final int id;

    Genre(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Genre fromId(int id) {
        for (Genre genre : Genre.values()) {
            if (genre.getId() == id) {
                return genre;
            }
        }
        throw new NotFoundException("Неизвестный ID жанра: " + id);
    }
}
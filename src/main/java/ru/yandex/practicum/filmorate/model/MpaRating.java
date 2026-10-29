
package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum MpaRating {
    G(1),
    PG(2),
    PG_13(3),
    R(4),
    NC_17(5);

    private final int id;

    MpaRating(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MpaRating fromId(int id) {
        for (MpaRating rating : MpaRating.values()) {
            if (rating.getId() == id) {
                return rating;
            }
        }
        throw new NotFoundException("Неизвестный ID рейтинга MPA: " + id);
    }
}

package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Не удалось создать пользоваетеля: email строка пустая или содержит неверный формат.");
            throw new ValidationException("Email строка пустая или содержит неверный формат.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Не удалось создать пользоваетеля: логин пустой или содержит пробелы.");
            throw new ValidationException("Логин не может быть пустым или содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Не удалось создать пользоваетеля: некорректная дата рождения.");
                throw new ValidationException("Выбрана некорректная дата рождения.");
            }
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь '{}' с именем '{}' добавлен, логин: {}", user.getId(), user.getName(), user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        if (newUser.getId() == 0) {
            log.warn("Не удалось обновить пользователя: Id должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser = users.get(newUser.getId());

        if (oldUser == null) {
            log.warn("Не удалось обновить пользователя: пользователь не найден.");
            throw new NotFoundException("Пользователь не найден");
        }
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.warn("Не удалось обновить пользователя: отсутствует email или содержит неверный формат.");
                throw new ValidationException("Email строка пустая или содержит неверный формат.");
            }
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                log.warn("Не удалось обновить пользователя: пустой логин или содержит пробелы.");
                throw new ValidationException("Логин не может быть пустым или содержать пробелы.");
            }
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                oldUser.setName(oldUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
        }

        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Не удалось обновить пользователя: некорректная дата рождения.");
                throw new ValidationException("Выбрана некорректная дата рождения.");
            }
            oldUser.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь под ID {} - успешно обновлён", oldUser.getId());
        return oldUser;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

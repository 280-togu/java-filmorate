package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    public User create(User user) {
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
        userStorage.addUser(user);
        log.info("Пользователь '{}' с именем '{}' добавлен, логин: {}", user.getId(), user.getName(), user.getLogin());
        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == 0) {
            log.warn("Не удалось обновить пользователя: Id должен быть указан.");
            throw new ValidationException("Id должен быть указан");
        }
        User oldUser = userStorage.getUserById(newUser.getId());

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
        userStorage.updateUser(oldUser);
        return oldUser;
    }

    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User userFriend = userStorage.getUserById(friendId);

        if (user == null) {
            throw new NotFoundException("Данного пользователся не существует.");
        }
        if (userFriend == null) {
            throw new NotFoundException("Пользователя с таким id не существует.");
        }
        user.getFriends().add(friendId);
        userFriend.getFriends().add(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(userFriend);
        return user;
    }

    public User removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User userFriend = userStorage.getUserById(friendId);

        if (user == null) {
            throw new NotFoundException("Данного пользователся не существует.");
        }
        if (userFriend == null) {
            throw new NotFoundException("Пользователя с таким id не существует.");
        }
        user.getFriends().remove(friendId);
        userFriend.getFriends().remove(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(userFriend);
        return user;
    }

    public Collection<User> mutualFriends(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User userFriend = userStorage.getUserById(friendId);
        if (user == null) {
            throw new NotFoundException("Данного пользователся не существует.");
        }
        if (userFriend == null) {
            throw new NotFoundException("Пользователя с таким id не существует.");
        }
        Set<Integer> friends = new HashSet<>(user.getFriends());

        friends.retainAll(userFriend.getFriends());

        Collection<User> mutualFriend = new HashSet<>();

        for (Integer friend : friends) {
            mutualFriend.add(userStorage.getUserById(friend));
        }
        return mutualFriend;
    }

    public User getUserById(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        return user;
    }

    public Collection<User> friendsFriend(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        Collection<User> friendsFriend = new HashSet<>();
        for (Integer friendId : user.getFriends()) {
            friendsFriend.add(userStorage.getUserById(friendId));
        }
        return friendsFriend;
    }

    private int getNextId() {
        int currentMaxId = userStorage.getAllUsers()
                .stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

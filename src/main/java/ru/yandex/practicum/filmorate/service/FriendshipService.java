package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendshipService {
    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);

    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    public FriendshipService(
            FriendshipStorage friendshipStorage,
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.friendshipStorage = friendshipStorage;
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) {
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            logger.warn("Не удалось добавить друга: один из пользователей не найден.");
            throw new NotFoundException("Один из пользователей не найден.");
        }
        if (userId == friendId) {
            logger.warn("Не удалось добавить друга: пользователь не может быть другом самому себе.");
            throw new ValidationException("Пользователь не может быть другом самому себе.");
        }
        if (friendshipStorage.getFriendship(userId, friendId).isPresent() || friendshipStorage.getFriendship(friendId, userId).isPresent()) {
            logger.warn("Не удалось добавить друга: дружба уже существует.");
            throw new ValidationException("Дружба уже существует.");
        }
        friendshipStorage.addFriendship(new Friendship(userId, friendId, false));
        logger.info("Пользователь {} добавил пользователя {} в друзья.", userId, friendId);
    }

    public void confirmFriendship(int userId, int friendId) {
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            logger.warn("Не удалось подтвердить дружбу: один из пользователей не найден.");
            throw new NotFoundException("Один из пользователей не найден.");
        }
        Friendship friendship = friendshipStorage.getFriendship(friendId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос дружбы не найден."));
        friendship.setConfirmed(true);
        friendshipStorage.updateFriendship(friendship);
        logger.info("Пользователь {} подтвердил дружбу с пользователем {}.", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            logger.warn("Не удалось удалить друга: один из пользователей не найден.");
            throw new NotFoundException("Один из пользователей не найден.");
        }
        friendshipStorage.deleteFriendship(userId, friendId);
        friendshipStorage.deleteFriendship(friendId, userId);
        logger.info("Пользователь {} удалил пользователя {} из друзей.", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        if (userStorage.getUserById(userId) == null) {
            logger.warn("Не удалось получить список друзей: пользователь не найден.");
            throw new NotFoundException("Пользователь не найден.");
        }
        List<Friendship> friendships = friendshipStorage.getFriendshipsByUserId(userId);
        return friendships.stream()
                .filter(Friendship::isConfirmed)
                .map(friendship -> {
                    int friendId = friendship.getUserId() == userId ? friendship.getFriendId() : friendship.getUserId();
                    return userStorage.getUserById(friendId);
                })
                .toList();
    }

    public List<User> getMutualFriends(int userId, int otherId) {
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(otherId) == null) {
            logger.warn("Не удалось получить общих друзей: один из пользователей не найден.");
            throw new NotFoundException("Один из пользователей не найден.");
        }
        List<User> userFriends = getFriends(userId);
        List<User> otherFriends = getFriends(otherId);
        Set<Integer> otherFriendIds = otherFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(user -> otherFriendIds.contains(user.getId()))
                .toList();
    }
}

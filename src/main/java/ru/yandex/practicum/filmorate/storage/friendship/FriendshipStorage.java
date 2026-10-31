package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipStorage {

    void addFriendship(Friendship friendship);

    Optional<Friendship> getFriendship(int userId, int friendId);

    List<Friendship> getFriendshipsByUserId(int userId);

    void updateFriendship(Friendship friendship);

    void deleteFriendship(int userId, int friendId);
}

package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {
    private final List<Friendship> friendships = new ArrayList<>();

    @Override
    public void addFriendship(Friendship friendship) {
        friendships.add(friendship);
    }

    @Override
    public Optional<Friendship> getFriendship(int userId, int friendId) {
        for (Friendship friendship : friendships) {
            if (friendship.getUserId() == userId && friendship.getFriendId() == friendId) {
                return Optional.of(friendship);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Friendship> getFriendshipsByUserId(int userId) {
        List<Friendship> result = new ArrayList<>();
        for (Friendship friendship : friendships) {
            if (friendship.getUserId() == userId || friendship.getFriendId() == userId) {
                result.add(friendship);
            }
        }
        return result;
    }

    @Override
    public void updateFriendship(Friendship friendship) {
        for (Friendship friendship1 : friendships) {
            if (friendship1.getUserId() == friendship.getUserId() && friendship1.getFriendId() == friendship.getFriendId()) {
                friendship1.setConfirmed(friendship.isConfirmed());
                return;
            }
        }
    }

    @Override
    public void deleteFriendship(int userId, int friendId) {
        friendships.removeIf(friendship -> friendship.getUserId() == userId && friendship.getFriendId() == friendId);
    }
}
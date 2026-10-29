package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addFriendship(Friendship friendship) {
        String sql = "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, friendship.getUserId(), friendship.getFriendId(), friendship.isConfirmed());
    }

    @Override
    public Optional<Friendship> getFriendship(int userId, int friendId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ? AND friend_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Friendship(
                        rs.getInt("user_id"),
                        rs.getInt("friend_id"),
                        rs.getBoolean("confirmed")
                ), userId, friendId).stream().findFirst();
    }

    @Override
    public List<Friendship> getFriendshipsByUserId(int userId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ? OR friend_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Friendship(
                        rs.getInt("user_id"),
                        rs.getInt("friend_id"),
                        rs.getBoolean("confirmed")
                ), userId, userId);
    }

    @Override
    public void updateFriendship(Friendship friendship) {
        String sql = "UPDATE friendships SET confirmed = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendship.isConfirmed(), friendship.getUserId(), friendship.getFriendId());
    }

    @Override
    public void deleteFriendship(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }
}

package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FriendshipDbStorage.class)
class FriendshipDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FriendshipDbStorage friendshipStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@example.com", "user1", "User One", null
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@example.com", "user2", "User Two", null
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                3, "user3@example.com", "user3", "User Three", null
        );
    }

    @Test
    void shouldAddFriendship() {
        Friendship friendship = new Friendship(1, 2, false);

        friendshipStorage.addFriendship(friendship);

        Optional<Friendship> result = friendshipStorage.getFriendship(1, 2);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getUserId());
        assertEquals(2, result.get().getFriendId());
        assertFalse(result.get().isConfirmed());
    }

    @Test
    void shouldReturnFriendshipByIds() {
        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                1, 2, true
        );

        Optional<Friendship> result = friendshipStorage.getFriendship(1, 2);

        assertTrue(result.isPresent());
        assertTrue(result.get().isConfirmed());
    }

    @Test
    void shouldReturnEmptyWhenFriendshipDoesNotExist() {
        Optional<Friendship> result = friendshipStorage.getFriendship(1, 2);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnAllFriendshipsForUser() {
        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                1, 2, true
        );
        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                3, 1, false
        );
        jdbcTemplate.update(
                "INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                2, 3, true
        );

        List<Friendship> result = friendshipStorage.getFriendshipsByUserId(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
        assertEquals(2, result.get(0).getFriendId());
        assertTrue(result.get(0).isConfirmed());
    }

    @Test
    void shouldUpdateFriendshipConfirmation() {
        friendshipStorage.addFriendship(new Friendship(1, 2, false));

        Friendship friendship = friendshipStorage.getFriendship(1, 2).orElseThrow();
        friendship.setConfirmed(true);

        friendshipStorage.updateFriendship(friendship);

        Optional<Friendship> result = friendshipStorage.getFriendship(1, 2);

        assertTrue(result.isPresent());
        assertTrue(result.get().isConfirmed());
    }

    @Test
    void shouldDeleteFriendship() {
        friendshipStorage.addFriendship(new Friendship(1, 2, false));

        friendshipStorage.deleteFriendship(1, 2);

        Optional<Friendship> result = friendshipStorage.getFriendship(1, 2);

        assertTrue(result.isEmpty());
    }
}
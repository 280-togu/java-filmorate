package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 3");
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@example.com", "user1", "User One",
                LocalDate.of(1990, 1, 10)
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@example.com", "user2", "User Two",
                LocalDate.of(1995, 5, 20)
        );
    }

    @Test
    void shouldReturnUserById() {
        User user = userStorage.getUserById(1);

        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("user1@example.com", user.getEmail());
        assertEquals("user1", user.getLogin());
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> users = userStorage.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldAddUser() {
        User user = new User();
        user.setEmail("new@example.com");
        user.setLogin("newuser");
        user.setName("New User");
        user.setBirthday(LocalDate.of(2000, 3, 15));

        userStorage.addUser(user);

        assertTrue(user.getId() > 0);

        User savedUser = userStorage.getUserById(user.getId());

        assertNotNull(savedUser);
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("newuser", savedUser.getLogin());
        assertEquals("New User", savedUser.getName());
        assertEquals(LocalDate.of(2000, 3, 15), savedUser.getBirthday());
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.getUserById(1);
        user.setEmail("updated@example.com");
        user.setLogin("updatedlogin");
        user.setName("Updated User");
        user.setBirthday(LocalDate.of(1992, 7, 12));

        userStorage.updateUser(user);

        User updatedUser = userStorage.getUserById(1);

        assertNotNull(updatedUser);
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Updated User", updatedUser.getName());
        assertEquals(LocalDate.of(1992, 7, 12), updatedUser.getBirthday());
    }

    @Test
    void shouldDeleteUser() {
        userStorage.deleteUser(1);

        assertNull(userStorage.getUserById(1));
        assertEquals(1, userStorage.getAllUsers().size());
    }
}
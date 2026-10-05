package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userService);
    }

    @Test
    void createUser_validUser_shouldCreateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        User result = controller.createUser(user);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void createUser_nullEmail_shouldThrowException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emptyEmail_shouldThrowException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emailWithoutAt_shouldThrowException() {
        User user = new User();
        user.setEmail("testmail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_nullLogin_shouldThrowException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin(null);
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emptyLogin_shouldThrowException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_loginWithSpace_shouldThrowException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test user");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_futureBirthday_shouldThrowException() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_nullName_shouldUseLoginAsName() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 5, 10));

        User result = controller.createUser(user);

        assertEquals("test", result.getName());
    }

    @Test
    void createUser_emptyName_shouldUseLoginAsName() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName("");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        User result = controller.createUser(user);

        assertEquals("test", result.getName());
    }

    @Test
    void createUser_emptyUser_shouldThrowException() {
        User user = new User();

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_birthdayToday_shouldCreateUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.now());

        User result = controller.createUser(user);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }
}
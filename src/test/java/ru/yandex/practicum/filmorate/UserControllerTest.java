package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void createUser_validUser_shouldCreateUser() {
        UserController controller = new UserController();

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
        UserController controller = new UserController();

        User user = new User();
        user.setEmail(null);
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emptyEmail_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emailWithoutAt_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("testmail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_nullLogin_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin(null);
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_emptyLogin_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_loginWithSpace_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test user");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1995, 5, 10));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_futureBirthday_shouldThrowException() {
        UserController controller = new UserController();

        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("test");
        user.setName("Test");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_nullName_shouldUseLoginAsName() {
        UserController controller = new UserController();

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
        UserController controller = new UserController();

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
        UserController controller = new UserController();

        User user = new User();

        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void createUser_birthdayToday_shouldCreateUser() {
        UserController controller = new UserController();

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
package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.friendship.InMemoryFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;
    private FriendshipService friendshipService;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFriendshipStorage friendshipStorage = new InMemoryFriendshipStorage();

        UserService userService = new UserService(userStorage);
        friendshipService = new FriendshipService(friendshipStorage, userStorage);

        controller = new UserController(userService, friendshipService);
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

    @Test
    void findUserById_existingUser_shouldReturnUser() {
        User createdUser = createTestUser("test");

        User result = controller.findUserById(createdUser.getId());

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("test", result.getLogin());
    }

    @Test
    void findUserById_nonExistingUser_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> controller.findUserById(999));
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        createTestUser("first");
        createTestUser("second");

        Collection<User> result = controller.findAll();

        assertEquals(2, result.size());
    }

    private User createTestUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1995, 5, 10));

        return controller.createUser(user);
    }

    @Test
    void addFriend_existingUsers_shouldCreateUnconfirmedFriendship() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");

        assertDoesNotThrow(
                () -> controller.addFriend(firstUser.getId(), secondUser.getId())
        );

        assertEquals(1, friendshipService.getFriends(firstUser.getId()).size());
        assertEquals(1, friendshipService.getFriends(secondUser.getId()).size());
    }

    @Test
    void addFriend_self_shouldThrowException() {
        User user = createTestUser("test");

        assertThrows(
                ValidationException.class,
                () -> controller.addFriend(user.getId(), user.getId())
        );
    }

    @Test
    void addFriend_nonExistingUser_shouldThrowException() {
        User user = createTestUser("test");

        assertThrows(
                NotFoundException.class,
                () -> controller.addFriend(user.getId(), 999)
        );
    }

    @Test
    void confirmFriendship_shouldConfirmFriendship() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");

        controller.addFriend(firstUser.getId(), secondUser.getId());

        controller.confirmFriendship(secondUser.getId(), firstUser.getId());

        Collection<User> firstUserFriends = controller.getFriends(firstUser.getId());
        Collection<User> secondUserFriends = controller.getFriends(secondUser.getId());

        assertEquals(1, firstUserFriends.size());
        assertEquals(secondUser.getId(), firstUserFriends.iterator().next().getId());

        assertEquals(1, secondUserFriends.size());
        assertEquals(firstUser.getId(), secondUserFriends.iterator().next().getId());
    }

    @Test
    void getFriends_shouldReturnConfirmedFriends() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");

        controller.addFriend(firstUser.getId(), secondUser.getId());
        controller.confirmFriendship(secondUser.getId(), firstUser.getId());

        Collection<User> firstUserFriends = controller.getFriends(firstUser.getId());
        Collection<User> secondUserFriends = controller.getFriends(secondUser.getId());

        assertEquals(1, firstUserFriends.size());
        assertEquals(secondUser.getId(), firstUserFriends.iterator().next().getId());

        assertEquals(1, secondUserFriends.size());
        assertEquals(firstUser.getId(), secondUserFriends.iterator().next().getId());
    }

    @Test
    void deleteFriend_existingFriendship_shouldRemoveFriendship() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");

        controller.addFriend(firstUser.getId(), secondUser.getId());
        controller.confirmFriendship(secondUser.getId(), firstUser.getId());

        assertEquals(1, controller.getFriends(firstUser.getId()).size());
        assertEquals(1, controller.getFriends(secondUser.getId()).size());

        assertDoesNotThrow(
                () -> controller.deleteFriend(firstUser.getId(), secondUser.getId())
        );

        assertTrue(controller.getFriends(firstUser.getId()).isEmpty());
        assertTrue(controller.getFriends(secondUser.getId()).isEmpty());
    }

    @Test
    void getCommonFriends_shouldReturnMutualFriends() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");
        User commonFriend = createTestUser("common");

        controller.addFriend(firstUser.getId(), commonFriend.getId());
        controller.addFriend(secondUser.getId(), commonFriend.getId());

        controller.confirmFriendship(commonFriend.getId(), firstUser.getId());
        controller.confirmFriendship(commonFriend.getId(), secondUser.getId());

        Collection<User> result =
                controller.getCommonFriends(firstUser.getId(), secondUser.getId());

        assertEquals(1, result.size());
        assertEquals(commonFriend.getId(), result.iterator().next().getId());
    }

    @Test
    void getFriends_unconfirmedFriendship_shouldReturnFriend() {
        User firstUser = createTestUser("first");
        User secondUser = createTestUser("second");

        controller.addFriend(firstUser.getId(), secondUser.getId());

        Collection<User> result = controller.getFriends(firstUser.getId());

        assertEquals(1, result.size());
        assertEquals(secondUser.getId(), result.iterator().next().getId());
    }
}
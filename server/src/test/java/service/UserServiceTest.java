package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;

public class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        // Clear the user table to ensure no leftover data interferes with tests.
        userDAO.clear();
        // Optionally clear auth if needed:
        // authDAO.clear();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Register User - Positive")
    public void testRegisterUserPositive() throws Exception {
        // Use a unique username each time.
        String username = "testUser_" + System.currentTimeMillis();
        String password = "testPassword";
        String email = "test@example.com";

        String authToken = userService.registerUser(username, password, email);
        assertNotNull(authToken, "Auth token should be generated on registration");

        UserData user = userDAO.getUser(username);
        assertNotNull(user, "User should exist in database");
        assertEquals(username, user.getUsername(), "Usernames should match");
        assertEquals(email, user.getEmail(), "Emails should match");

        // The stored password should be hashed and not equal to the clear text.
        assertNotEquals(password, user.getPassword(), "Stored password should not be clear text");
        assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"),
                "Password should be hashed using BCrypt");
    }

    @Test
    @DisplayName("Register User - Negative (Missing Fields)")
    public void testRegisterUserMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("user", "", "email@example.com");
        });
        assertEquals("Bad request: Missing fields.", ex.getMessage());
    }

    @Test
    @DisplayName("Register User - Negative (Duplicate Username)")
    public void testRegisterUserDuplicate() {
        // Use a unique username.
        String username = "duplicateUser_" + System.currentTimeMillis();
        String password = "password";
        String email = "email@example.com";

        // First registration should succeed.
        userService.registerUser(username, password, email);

        // Second registration with the same username should fail.
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(username, password, email);
        });
        assertEquals("Username already taken.", ex.getMessage());
    }

    @Test
    @DisplayName("Login User - Positive")
    public void testLoginUserPositive() {
        String username = "loginUser_" + System.currentTimeMillis();
        String password = "loginPass";
        String email = "login@example.com";
        userService.registerUser(username, password, email);

        String authToken = userService.loginUser(username, password);
        assertNotNull(authToken, "Login should return an auth token");
    }

    @Test
    @DisplayName("Login User - Negative (Invalid Password)")
    public void testLoginUserInvalidPassword() {
        String username = "loginUserInvalid_" + System.currentTimeMillis();
        String password = "correctPass";
        String email = "invalid@example.com";
        userService.registerUser(username, password, email);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser(username, "wrongPass");
        });
        assertEquals("Invalid username or password.", ex.getMessage());
    }

    @Test
    @DisplayName("Login User - Negative (Non-existent User)")
    public void testLoginUserNonExistent() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("nonexistentUser", "anyPassword");
        });
        assertEquals("Invalid username or password.", ex.getMessage());
    }

    @Test
    @DisplayName("Logout User - Negative (Invalid Auth Token)")
    public void testLogoutUserInvalid() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.logoutUser("invalidToken");
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }
}

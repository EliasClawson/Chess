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
        // Optionally, if needed, clear auth as well:
        // authDAO.clear();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Register User - Positive")
    public void testRegisterUserPositive() throws Exception {
        // Use a unique username.
        String username = "testUser_" + System.currentTimeMillis();
        String password = "testPassword";
        String email = "test@example.com";

        String authToken = userService.registerUser(username, password, email);
        assertNotNull(authToken, "Auth token should be generated on registration");

        UserData user = userDAO.getUser(username);
        assertNotNull(user, "User should exist in database");
        assertEquals(username, user.getUsername(), "Usernames should match");
        assertEquals(email, user.getEmail(), "Emails should match");
        // Check that the stored password is hashed.
        assertNotEquals(password, user.getPassword(), "Stored password should not be clear text");
        assertTrue(user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$"),
                "Password should be hashed using BCrypt");
    }

    @Test
    @DisplayName("Register User - Negative (Missing Fields)")
    public void testRegisterUserMissingFields() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("user", "", "email@example.com");
        });
        assertEquals("Bad request: Missing fields.", ex.getMessage());
    }

    @Test
    @DisplayName("Register User - Negative (Duplicate Username)")
    public void testRegisterUserDuplicate() throws Exception {
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
    public void testLoginUserPositive() throws Exception {
        String username = "loginUser_" + System.currentTimeMillis();
        String password = "loginPass";
        String email = "login@example.com";
        userService.registerUser(username, password, email);

        String authToken = userService.loginUser(username, password);
        assertNotNull(authToken, "Login should return an auth token");
    }

    @Test
    @DisplayName("Login User - Negative (Invalid Password)")
    public void testLoginUserInvalidPassword() throws Exception {
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
    public void testLoginUserNonExistent() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("nonexistentUser", "anyPassword");
        });
        assertEquals("Invalid username or password.", ex.getMessage());
    }

    @Test
    @DisplayName("Logout User - Negative (Invalid Auth Token)")
    public void testLogoutUserInvalid() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.logoutUser("invalidToken");
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }

    @Test
    @DisplayName("Logout User - Positive")
    public void testLogoutUserPositive() throws Exception {
        String username = "logoutUser_" + System.currentTimeMillis();
        String password = "logoutPass";
        String email = "logout@example.com";
        // Register and login the user.
        userService.registerUser(username, password, email);
        String loginToken = userService.loginUser(username, password);
        assertNotNull(loginToken, "Login should return a valid auth token");
        // Logout should succeed.
        assertDoesNotThrow(() -> userService.logoutUser(loginToken));
        // A subsequent logout attempt with the same token should fail.
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.logoutUser(loginToken);
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }
}

package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;

public class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception{
        // Create fresh instances for isolation between tests.
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    // ----- registerUser Tests -----

    @Test
    public void testRegisterUserPositive() throws Exception{
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        String authToken = userService.registerUser(username, password, email);

        // Assert that an auth token is returned.
        assertNotNull(authToken, "Auth token should not be null for successful registration");
        assertFalse(authToken.trim().isEmpty(), "Auth token should not be empty");
    }

    @Test
    public void testRegisterUserMissingFieldNegative() throws Exception{
        String username = "testUser";
        String password = null; // Missing password
        String email = "test@example.com";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(username, password, email);
        });
        assertEquals("Bad request: Missing fields.", exception.getMessage());
    }

    @Test
    public void testRegisterUserDuplicateNegative() throws Exception{
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        // Register the user once.
        userService.registerUser(username, password, email);

        // Attempt to register the same username again.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(username, "newPass", "new@example.com");
        });
        assertEquals("Username already taken.", exception.getMessage());
    }

    // ----- loginUser Tests -----

    @Test
    public void testLoginUserPositive() throws Exception{
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        // First, register the user.
        userService.registerUser(username, password, email);

        // Then, login with the correct credentials.
        String authToken = userService.loginUser(username, password);
        assertNotNull(authToken, "Auth token should be returned upon successful login");
    }

    @Test
    public void testLoginUserNegativeInvalidPassword() throws Exception{
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        // Register the user.
        userService.registerUser(username, password, email);

        // Attempt login with the wrong password.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser(username, "wrongPass");
        });
        assertEquals("Invalid username or password.", exception.getMessage());
    }

    @Test
    public void testLoginUserNegativeNonExistentUser() throws Exception{
        // Attempt login with a username that doesn't exist.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("nonExistent", "anyPass");
        });
        assertEquals("Invalid username or password.", exception.getMessage());
    }

    // ----- logoutUser Tests -----

    @Test
    public void testLogoutUserPositive() throws Exception {
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        // Register and login the user.
        String authToken = userService.registerUser(username, password, email);
        // Ensure the token exists in AuthDAO.
        assertNotNull(authDAO.getAuth(authToken), "Auth token should exist before logout");

        // Logout.
        userService.logoutUser(authToken);

        // After logout, the token should be removed.
        assertNull(authDAO.getAuth(authToken), "Auth token should be removed after logout");
    }

    @Test
    public void testLogoutUserNegativeInvalidToken() throws Exception{
        // Attempt to logout with an invalid token.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.logoutUser("invalidToken");
        });
        assertEquals("Invalid auth token.", exception.getMessage());
    }
}
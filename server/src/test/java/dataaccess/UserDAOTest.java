package dataaccess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import model.UserData;

import java.sql.Connection;

public class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize your DAO before each test.
        userDAO = new UserDAO();

        // Optionally, clear the table if your DAO supports that.
        userDAO.clear();
    }

    @Test
    @DisplayName("Test Create and Retrieve User")
    public void testCreateAndGetUser() throws Exception {
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        // Create the user.
        userDAO.createUser(username, password, email);

        // Retrieve the user.
        UserData user = userDAO.getUser(username);
        assertNotNull(user, "User should be retrieved successfully.");
        assertEquals(username, user.getUsername(), "Usernames should match.");
        assertEquals(email, user.getEmail(), "Emails should match.");

        // The stored password should be hashed.
        assertNotEquals(password, user.getPassword(), "Stored password should not match clear text.");
        assertTrue(user.getPassword().startsWith("$2a$"), "Password should be hashed using BCrypt.");
    }

    @Test
    @DisplayName("Test Duplicate User Creation Fails")
    public void testDuplicateUserFails() throws Exception {
        String username = "testUser";
        String password = "testPass";
        String email = "test@example.com";

        userDAO.createUser(username, password, email);

        Exception exception = assertThrows(Exception.class, () -> {
            // Try to create the same user again.
            userDAO.createUser(username, password, email);
        });
        // You can check the exception message if you want.
        String expectedMessage = "Error creating user";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Test Clear Method")
    public void testClearUsers() throws Exception {
        // Create a user.
        userDAO.createUser("user1", "pass1", "email1@example.com");
        userDAO.createUser("user2", "pass2", "email2@example.com");

        // Ensure users exist.
        assertNotNull(userDAO.getUser("user1"));
        assertNotNull(userDAO.getUser("user2"));

        // Clear the table.
        userDAO.clear();

        // Check that the table is empty.
        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }

    @Test
    @DisplayName("Test Get User Not Found")
    public void testGetUserNotFound() throws Exception {
        UserData user = userDAO.getUser("nonExistentUser");
        assertNull(user, "Expected null for a non-existent user");
    }

    @Test
    @DisplayName("Test Create User With Missing Fields")
    public void testCreateUserMissingFields() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            userDAO.createUser("", "password", "email@example.com");
        });
        // Optionally, check the exception message if applicable.
    }

    @Test
    @DisplayName("Test User Exists Method")
    public void testUserExists() throws Exception {
        // Before creating, userExists should return false.
        assertFalse(userDAO.userExists("unknownUser"), "User should not exist initially");
        // After creating a user, userExists should return true.
        userDAO.createUser("newUser", "newPass", "new@example.com");
        assertTrue(userDAO.userExists("newUser"), "User should exist after creation");
    }

    @Test
    @DisplayName("Test Get Connection")
    public void testGetConnection() throws Exception {
        Connection conn = DatabaseManager.getConnection();
        assertNotNull(conn, "DatabaseManager should return a valid connection");
        conn.close();
    }


}

package dataaccess;

import static org.junit.jupiter.api.Assertions.*;

import model.AuthData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDAOTest {

    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() throws Exception {
        authDAO = new AuthDAO();
        // Clear auth tokens before each test.
        authDAO.clear();
    }

    @Test
    @DisplayName("Test Create and Get Auth Token")
    public void testCreateAndGetAuth() throws Exception {
        String username = "testUser";
        String token = authDAO.createAuth(username);
        assertNotNull(token, "Auth token should be generated");

        AuthData authData = authDAO.getAuth(token);
        assertNotNull(authData, "AuthData should be retrieved");
        assertEquals(username, authData.getUsername(), "Username should match");
    }

    @Test
    @DisplayName("Test Delete Auth Token")
    public void testDeleteAuth() throws Exception {
        String username = "testUser";
        String token = authDAO.createAuth(username);
        assertNotNull(authDAO.getAuth(token), "AuthData should exist before deletion");

        authDAO.deleteAuth(token);
        assertNull(authDAO.getAuth(token), "AuthData should be null after deletion");
    }

    @Test
    @DisplayName("Test Clear Auth Tokens")
    public void testClearAuth() throws Exception {
        // Create multiple auth tokens.
        authDAO.createAuth("user1");
        authDAO.createAuth("user2");

        // Confirm tokens exist.
        assertNotNull(authDAO.getAuth(authDAO.createAuth("user3")), "Auth token should be created");

        // Clear all tokens.
        authDAO.clear();

        // After clearing, getting any auth token should return null.
        // Since we don't know specific tokens, we'll try retrieving one token that we just created.
        String token = authDAO.createAuth("user4");
        authDAO.clear();
        assertNull(authDAO.getAuth(token), "Auth tokens should be cleared");
    }

    @Test
    @DisplayName("Test Get Auth with Invalid Token")
    public void testGetAuthInvalidToken() throws Exception {
        AuthData authData = authDAO.getAuth("nonExistentToken");
        assertNull(authData, "Expected null for non-existent token");
    }

    @Test
    @DisplayName("Test Delete Non-Existent Auth Token")
    public void testDeleteNonExistentAuth() throws Exception {
        // Expect no exception when deleting a token that doesn't exist.
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonExistentToken"), "Deleting a non-existent token should not throw an exception");
    }

}

package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import server.Server;
import model.AuthData;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static int port; // store the server port

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // Clear the database before each test by sending a DELETE request to /db.
    @BeforeEach
    public void clearDatabase() throws Exception {
        URL url = new URL("http://localhost:" + port + "/db");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Failed to clear database, response code: " + responseCode);
        }
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    // ------------------ Positive Tests ------------------

    @Test
    void registerTest() throws Exception {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        assertNotNull(authData);
        assertNotNull(authData.getAuthToken());
        // The token should be longer than 10 characters.
        assertTrue(authData.getAuthToken().length() > 10);
    }

    @Test
    void loginTest() throws Exception {
        // First, register a user.
        facade.register("player2", "password", "player2@email.com");
        AuthData authData = facade.login("player2", "password");
        assertNotNull(authData);
        assertNotNull(authData.getAuthToken());
        assertTrue(authData.getAuthToken().length() > 10);
    }

    @Test
    void logoutTest() throws Exception {
        AuthData authData = facade.register("player3", "password", "player3@email.com");
        // logout should succeed without throwing an exception.
        assertDoesNotThrow(() -> facade.logout(authData.getAuthToken()));
    }

    @Test
    void createGameTest() throws Exception {
        AuthData authData = facade.register("player4", "password", "player4@email.com");
        int gameID = facade.createGame(authData.getAuthToken(), "Test Game");
        assertTrue(gameID > 0, "GameID should be a positive integer");
    }

    @Test
    void listGamesTest() throws Exception {
        AuthData authData = facade.register("player5", "password", "player5@email.com");
        // Create a game so that listGames returns at least one game.
        facade.createGame(authData.getAuthToken(), "List Game Test");
        List<?> games = facade.listGames(authData.getAuthToken());
        assertNotNull(games);
        assertFalse(games.isEmpty(), "List of games should not be empty");
    }

    @Test
    void joinGameTest() throws Exception {
        AuthData authData = facade.register("player6", "password", "player6@email.com");
        int gameID = facade.createGame(authData.getAuthToken(), "Join Game Test");
        // Attempt to join the game as white. Test passes if no exception is thrown.
        assertDoesNotThrow(() -> facade.joinGame(authData.getAuthToken(), gameID, true));
    }

    // ------------------ Negative Tests ------------------

    // Login negative: wrong password
    @Test
    void loginWithWrongPasswordTest() throws Exception {
        facade.register("wrongPassUser", "correctPassword", "wp@example.com");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.login("wrongPassUser", "incorrectPassword");
        });
        assertTrue(ex.getMessage().contains("Invalid"), "Expected invalid credentials error.");
    }

    // Logout negative: invalid auth token
    @Test
    void logoutWithInvalidTokenTest() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.logout("thisIsAnInvalidToken");
        });
        assertTrue(ex.getMessage().contains("Invalid"), "Expected invalid token error on logout.");
    }

    // Create game negative: invalid auth token
    @Test
    void createGameWithInvalidTokenTest() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.createGame("invalidToken", "Game With Invalid Token");
        });
        assertTrue(ex.getMessage().contains("Invalid"), "Expected invalid token error on create game.");
    }

    // List games negative: invalid auth token
    @Test
    void listGamesWithInvalidTokenTest() throws Exception {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.listGames("invalidToken");
        });
        assertTrue(ex.getMessage().contains("Invalid"), "Expected invalid token error on list games.");
    }

    // Join game negative: invalid auth token
    @Test
    void joinGameWithInvalidTokenTest() throws Exception {
        // Create a valid game first.
        AuthData auth = facade.register("validUserForJoin", "password", "validjoin@example.com");
        int gameID = facade.createGame(auth.getAuthToken(), "Game for Join Invalid Token Test");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.joinGame("invalidToken", gameID, true);
        });
        assertTrue(ex.getMessage().contains("Invalid"), "Expected invalid token error on join game.");
    }
}

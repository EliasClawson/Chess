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

    // Additional Tests for error conditions:

    @Test
    void duplicateJoinTest() throws Exception {
        // Register one user and create a game.
        AuthData auth = facade.register("dupPlayer", "password", "dup@example.com");
        int gameID = facade.createGame(auth.getAuthToken(), "Duplicate Join Test");
        // Join as white.
        facade.joinGame(auth.getAuthToken(), gameID, true);
        // Attempt to join again (even as black) should throw an exception.
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.joinGame(auth.getAuthToken(), gameID, false);
        });
        assertTrue(ex.getMessage().contains("already joined"), "Expected duplicate join error.");
    }

    @Test
    void joinGameWhiteSlotTakenTest() throws Exception {
        // Register first user and create a game.
        AuthData auth1 = facade.register("playerA", "password", "playerA@example.com");
        int gameID = facade.createGame(auth1.getAuthToken(), "White Slot Test");
        facade.joinGame(auth1.getAuthToken(), gameID, true);
        // Register second user and attempt to join as white (should fail).
        AuthData auth2 = facade.register("playerB", "password", "playerB@example.com");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.joinGame(auth2.getAuthToken(), gameID, true);
        });
        assertTrue(ex.getMessage().contains("White slot already taken"), "Expected white slot taken error.");
    }

    @Test
    void joinGameBlackSlotTakenTest() throws Exception {
        // Register first user, create a game, and join as black.
        AuthData auth1 = facade.register("playerC", "password", "playerC@example.com");
        int gameID = facade.createGame(auth1.getAuthToken(), "Black Slot Test");
        // First join as black with second user.
        AuthData auth2 = facade.register("playerD", "password", "playerD@example.com");
        facade.joinGame(auth2.getAuthToken(), gameID, false);
        // Now register third user and attempt to join as black (should fail).
        AuthData auth3 = facade.register("playerE", "password", "playerE@example.com");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.joinGame(auth3.getAuthToken(), gameID, false);
        });
        assertTrue(ex.getMessage().contains("Black slot already taken"), "Expected black slot taken error.");
    }

    @Test
    void joinNonExistentGameTest() throws Exception {
        AuthData auth = facade.register("nonexistentPlayer", "password", "nonexistent@example.com");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.joinGame(auth.getAuthToken(), 9999, true);
        });
        assertTrue(ex.getMessage().contains("Game not found"), "Expected game not found error.");
    }

    @Test
    void duplicateRegisterTest() throws Exception {
        facade.register("duplicateUser", "password", "dup@example.com");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            facade.register("duplicateUser", "password", "dup@example.com");
        });
        assertTrue(ex.getMessage().contains("already taken"), "Expected duplicate registration error.");
    }
}

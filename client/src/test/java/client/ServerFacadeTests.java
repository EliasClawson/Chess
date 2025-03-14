package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import server.Server;
import model.AuthData;
import java.util.List;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
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
}

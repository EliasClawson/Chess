package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import model.GameData;
import chess.ChessGame;
import java.util.List;

public class GameServiceTest {

    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService gameService;

    // We'll use a valid user for our tests
    private final String validUser = "testUser";
    private String validAuthToken;
    private final String invalidAuthToken = "invalidToken";

    @BeforeEach
    public void setUp() throws Exception{
        // Create new instances for each test for isolation.
        gameDAO = new GameDAO();
        authDAO = new AuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        // Create a valid auth token for a test user.
        validAuthToken = authDAO.createAuth(validUser);
    }

    // ----- Tests for createGame -----

    @Test
    public void testCreateGamePositive() throws Exception{
        String gameName = "Test Game";
        int gameID = gameService.createGame(validAuthToken, gameName);
        // Check that the game is created in the DAO.
        GameData createdGame = gameDAO.getGame(gameID);
        assertNotNull(createdGame, "Game should be created");
        assertEquals(gameName, createdGame.getGameName(), "Game name should match");
    }

    @Test
    public void testCreateGameInvalidAuthNegative() throws Exception{
        String gameName = "Test Game";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(invalidAuthToken, gameName);
        });
        assertEquals("Invalid auth token.", exception.getMessage());
    }

    // ----- Tests for listGames -----

    @Test
    public void testListGamesPositive() throws Exception{
        // Create two games.
        gameService.createGame(validAuthToken, "Game 1");
        gameService.createGame(validAuthToken, "Game 2");

        List<GameData> games = gameService.listGames(validAuthToken);
        assertTrue(games.size() >= 2, "There should be at least 2 games");
        // Now checks for >= instead of ==
    }

    @Test
    public void testListGamesInvalidAuthNegative() throws Exception{
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.listGames(invalidAuthToken);
        });
        assertEquals("Invalid auth token.", exception.getMessage());
    }

    // ----- Tests for joinGame -----

    @Test
    public void testJoinGamePositiveWhite() throws Exception{
        String gameName = "Join Game White";
        int gameID = gameService.createGame(validAuthToken, gameName);

        // Create a second user and get an auth token.
        String secondAuthToken = authDAO.createAuth("secondUser");

        // Join the game as white.
        gameService.joinGame(secondAuthToken, gameID, true);
        GameData game = gameDAO.getGame(gameID);
        assertEquals("secondUser", game.getWhiteUsername(), "White slot should be filled with secondUser");
    }

    @Test
    public void testJoinGamePositiveBlack() throws Exception{
        String gameName = "Join Game Black";
        int gameID = gameService.createGame(validAuthToken, gameName);

        // Create a second user and get an auth token.
        String secondAuthToken = authDAO.createAuth("secondUser");

        // Join the game as black.
        gameService.joinGame(secondAuthToken, gameID, false);
        GameData game = gameDAO.getGame(gameID);
        assertEquals("secondUser", game.getBlackUsername(), "Black slot should be filled with secondUser");
    }

    @Test
    public void testJoinGameInvalidAuthNegative() throws Exception{
        String gameName = "Join Game Invalid Auth";
        int gameID = gameService.createGame(validAuthToken, gameName);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(invalidAuthToken, gameID, true);
        });
        assertEquals("Invalid auth token.", exception.getMessage());
    }

    @Test
    public void testJoinGameGameNotFoundNegative() throws Exception{
        // Create a valid auth token for a second user.
        String secondAuthToken = authDAO.createAuth("secondUser");

        // Attempt to join a game with an ID that doesn't exist.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(secondAuthToken, 999, true); // 999 assumed non-existent.
        });
        assertEquals("Game not found.", exception.getMessage());
    }

    @Test
    public void testJoinGameSlotAlreadyTakenNegativeWhite() throws Exception{
        String gameName = "Join Game Slot Taken White";
        int gameID = gameService.createGame(validAuthToken, gameName);

        String secondAuthToken = authDAO.createAuth("secondUser");
        // First join as white.
        gameService.joinGame(secondAuthToken, gameID, true);

        // Another user trying to join white.
        String thirdAuthToken = authDAO.createAuth("thirdUser");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(thirdAuthToken, gameID, true);
        });
        assertEquals("White slot already taken.", exception.getMessage());
    }

    @Test
    public void testJoinGameSlotAlreadyTakenNegativeBlack() throws Exception{
        String gameName = "Join Game Slot Taken Black";
        int gameID = gameService.createGame(validAuthToken, gameName);

        String secondAuthToken = authDAO.createAuth("secondUser");
        // First join as black.
        gameService.joinGame(secondAuthToken, gameID, false);

        // Another user trying to join black.
        String thirdAuthToken = authDAO.createAuth("thirdUser");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(thirdAuthToken, gameID, false);
        });
        assertEquals("Black slot already taken.", exception.getMessage());
    }
}

package service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import model.GameData;
import chess.ChessGame;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTest {

    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService gameService;
    
    // Use a valid test user for authentication.
    private final String validUser = "gameTestUser_" + System.currentTimeMillis();
    private String validAuthToken;
    private final String invalidAuthToken = "invalidToken";

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize DAOs.
        gameDAO = new GameDAO();
        authDAO = new AuthDAO();
        // Clear existing game and auth data.
        gameDAO.clear();
        authDAO.clear();
        // Create a valid auth token by simulating a registration.
        validAuthToken = authDAO.createAuth(validUser);
        // Initialize GameService.
        gameService = new GameService(gameDAO, authDAO);
    }

    @AfterEach
    public void tearDown() throws Exception {
        gameDAO.clear();
        authDAO.clear();
    }

    @Test
    @DisplayName("Create Game - Positive")
    public void testCreateGamePositive() {
        String gameName = "Service Test Game";
        int gameID = gameService.createGame(validAuthToken, gameName);
        assertTrue(gameID > 0, "Game ID should be greater than 0 on creation");
        
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game, "Game should be retrievable from DAO");
        assertEquals(gameName, game.getGameName(), "Game name should match");
    }
    
    @Test
    @DisplayName("Create Game - Negative (Invalid Auth)")
    public void testCreateGameInvalidAuth() {
        String gameName = "Invalid Auth Game";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(invalidAuthToken, gameName);
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }
    
    @Test
    @DisplayName("List Games - Positive")
    public void testListGamesPositive() {
        // Create two games.
        gameService.createGame(validAuthToken, "Game One");
        gameService.createGame(validAuthToken, "Game Two");
        
        List<GameData> games = gameService.listGames(validAuthToken);
        assertTrue(games.size() >= 2, "There should be at least 2 games listed");
    }
    
    @Test
    @DisplayName("List Games - Negative (Invalid Auth)")
    public void testListGamesInvalidAuth() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            gameService.listGames(invalidAuthToken);
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }
    
    @Test
    @DisplayName("Join Game - Positive (White)")
    public void testJoinGamePositiveWhite() {
        String gameName = "Join Game Test";
        int gameID = gameService.createGame(validAuthToken, gameName);
        
        // Create a second user for joining.
        String secondUser = "secondUser_" + System.currentTimeMillis();
        String secondAuthToken = authDAO.createAuth(secondUser);
        
        // Join the game as white.
        assertDoesNotThrow(() -> gameService.joinGame(secondAuthToken, gameID, true));
        
        GameData game = gameDAO.getGame(gameID);
        assertEquals(secondUser, game.getWhiteUsername(), "White slot should be filled by second user");
    }
    
    @Test
    @DisplayName("Join Game - Negative (Invalid Auth)")
    public void testJoinGameInvalidAuth() {
        String gameName = "Join Game Test Invalid Auth";
        int gameID = gameService.createGame(validAuthToken, gameName);
        
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(invalidAuthToken, gameID, true);
        });
        assertEquals("Invalid auth token.", ex.getMessage());
    }
    
    @Test
    @DisplayName("Join Game - Negative (Game Not Found)")
    public void testJoinGameNotFound() {
        // Create a valid auth for a user.
        String secondUser = "secondUser_" + System.currentTimeMillis();
        String secondAuthToken = authDAO.createAuth(secondUser);
        
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(secondAuthToken, 9999, true); // Assuming 9999 does not exist.
        });
        assertEquals("Game not found.", ex.getMessage());
    }
    
    @Test
    @DisplayName("Join Game - Negative (Slot Already Taken)")
    public void testJoinGameSlotTaken() {
        String gameName = "Join Game Duplicate Slot";
        int gameID = gameService.createGame(validAuthToken, gameName);
        
        // Create a second user to join.
        String secondUser = "secondUser_" + System.currentTimeMillis();
        String secondAuthToken = authDAO.createAuth(secondUser);
        
        // First join as white.
        gameService.joinGame(secondAuthToken, gameID, true);
        
        // A third user attempts to join the white slot.
        String thirdUser = "thirdUser_" + System.currentTimeMillis();
        String thirdAuthToken = authDAO.createAuth(thirdUser);
        
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(thirdAuthToken, gameID, true);
        });
        assertEquals("White slot already taken.", ex.getMessage());
    }
}

package dataaccess;

import static org.junit.jupiter.api.Assertions.*;

import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTest {

    private GameDAO gameDAO;

    @BeforeEach
    public void setUp() throws Exception {
        gameDAO = new GameDAO();
        // Clear games before each test.
        gameDAO.clear();
    }

    @Test
    @DisplayName("Test Create and Retrieve Game")
    public void testCreateAndGetGame() throws Exception {
        String gameName = "Unit Test Game";
        int gameID = gameDAO.createGame(gameName);
        GameData game = gameDAO.getGame(gameID);

        assertNotNull(game, "Game should be retrieved after creation");
        assertEquals(gameID, game.getGameID(), "Game ID should match");
        assertEquals(gameName, game.getGameName(), "Game name should match");
        // Initially, no players have joined.
        assertNull(game.getWhiteUsername(), "White username should be null initially");
        assertNull(game.getBlackUsername(), "Black username should be null initially");
        // Check that the game state is not null.
        assertNotNull(game.getGame(), "Game state should be initialized");
    }

    @Test
    @DisplayName("Test List Games")
    public void testListGames() throws Exception {
        // Create two games.
        int gameID1 = gameDAO.createGame("Game One");
        int gameID2 = gameDAO.createGame("Game Two");

        Map<Integer, GameData> games = gameDAO.listGames();
        assertTrue(games.size() >= 2, "List of games should have at least 2 entries");
        assertTrue(games.containsKey(gameID1), "List should contain gameID1");
        assertTrue(games.containsKey(gameID2), "List should contain gameID2");
    }

    @Test
    @DisplayName("Test Update Game")
    public void testUpdateGame() throws Exception {
        // Create a game.
        String gameName = "Update Test Game";
        int gameID = gameDAO.createGame(gameName);
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game, "Game should exist");

        // Simulate joining the game (set white username).
        GameData updatedGame = new GameData(game.getGameID(), "PlayerWhite", game.getBlackUsername(), game.getGameName(), game.getGame());
        gameDAO.updateGame(updatedGame);

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved, "Updated game should be retrieved");
        assertEquals("PlayerWhite", retrieved.getWhiteUsername(), "White username should be updated");
    }

    @Test
    @DisplayName("Test Clear Games")
    public void testClearGames() throws Exception {
        // Create some games.
        gameDAO.createGame("Game A");
        gameDAO.createGame("Game B");

        Map<Integer, GameData> gamesBefore = gameDAO.listGames();
        assertTrue(gamesBefore.size() >= 2, "At least 2 games should exist");

        // Clear games.
        gameDAO.clear();

        Map<Integer, GameData> gamesAfter = gameDAO.listGames();
        assertEquals(0, gamesAfter.size(), "All games should be cleared");
    }
}

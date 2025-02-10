package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    // Create a new game and return the ID
    public int createGame(String gameName) {
        int gameID = nextGameID++;
        GameData game = new GameData(gameID, null, null, gameName, null); // No players yet
        games.put(gameID, game);
        return gameID;
    }

    // Retrieve a game by ID
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    // List all games
    public Map<Integer, GameData> listGames() {
        return games;
    }

    // Update a game (e.g., add players)
    public void updateGame(GameData game) {
        games.put(game.getGameID(), game);
    }
}
package dataaccess;

import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int currentGameID = 0;

    // Create a new game and return its gameID
    public int createGame(String gameName) {
        int gameID = ++currentGameID;
        // Assuming a new game is created with no players yet and a new ChessGame instance.
        games.put(gameID, new GameData(gameID, null, null, gameName, new chess.ChessGame()));
        return gameID;
    }

    // List all games
    public Map<Integer, GameData> listGames() {
        return games;
    }

    // Retrieve a game by gameID
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    // Update an existing game
    public void updateGame(GameData game) {
        games.put(game.getGameID(), game);
    }

    // Clear all games and reset the gameID counter
    public void clear() {
        games.clear();
        currentGameID = 0;
    }
}
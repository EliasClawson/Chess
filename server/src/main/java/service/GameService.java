package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import java.util.Map;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // Create a new game
    public int createGame(String authToken, String gameName) {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        return gameDAO.createGame(gameName); // Return game ID
    }

    // List all games
    public Map<Integer, GameData> listGames() {
        return gameDAO.listGames();
    }

    // Join a game
    public void joinGame(String authToken, int gameID, boolean joinAsWhite) {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new IllegalArgumentException("Game not found.");
        }

        // Assign player to white or black
        if (joinAsWhite) {
            if (game.getWhiteUsername() != null) {
                throw new IllegalArgumentException("White slot already taken.");
            }
            game = new GameData(game.getGameID(), auth.getUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
        } else {
            if (game.getBlackUsername() != null) {
                throw new IllegalArgumentException("Black slot already taken.");
            }
            game = new GameData(game.getGameID(), game.getWhiteUsername(), auth.getUsername(), game.getGameName(), game.getGame());
        }

        gameDAO.updateGame(game);
    }
}
package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import java.util.List;
import java.util.ArrayList;
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
        // Validate auth token
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        // Create game in the DAO and return its ID
        return gameDAO.createGame(gameName);
    }

    // List all games (with auth check)
    public List<GameData> listGames(String authToken) {
        // Validate auth token
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        // Get the map of games and convert it to a list
        Map<Integer, GameData> gamesMap = gameDAO.listGames();
        return new ArrayList<>(gamesMap.values());
    }

    // Join a game: joinAsWhite indicates if the user wants the white slot
    public void joinGame(String authToken, int gameID, boolean joinAsWhite) {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new IllegalArgumentException("Game not found.");
        }

        if (joinAsWhite) {
            if (game.getWhiteUsername() != null) {
                throw new IllegalArgumentException("White slot already taken.");
            }
            game = new GameData(
                    game.getGameID(),
                    auth.getUsername(),
                    game.getBlackUsername(),
                    game.getGameName(),
                    game.getGame()
            );
        } else {
            if (game.getBlackUsername() != null) {
                throw new IllegalArgumentException("Black slot already taken.");
            }
            game = new GameData(
                    game.getGameID(),
                    game.getWhiteUsername(),
                    auth.getUsername(),
                    game.getGameName(),
                    game.getGame()
            );
        }

        gameDAO.updateGame(game);
    }
}
package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
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
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking auth token: " + e.getMessage(), e);
        }
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        try {
            return gameDAO.createGame(gameName);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error creating game: " + e.getMessage(), e);
        }
    }

    // List all games (with auth check)
    public List<GameData> listGames(String authToken) {
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking auth token: " + e.getMessage(), e);
        }
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        try {
            Map<Integer, GameData> gamesMap = gameDAO.listGames();
            return new ArrayList<>(gamesMap.values());
        } catch (DataAccessException e) {
            throw new RuntimeException("Error listing games: " + e.getMessage(), e);
        }
    }

    // Join a game: joinAsWhite indicates if the user wants the white slot
    public void joinGame(String authToken, int gameID, boolean joinAsWhite) {
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking auth token: " + e.getMessage(), e);
        }
        if (auth == null) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        try {
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
        } catch (DataAccessException e) {
            throw new RuntimeException("Error joining game: " + e.getMessage(), e);
        }
    }
}

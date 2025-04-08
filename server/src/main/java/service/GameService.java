package service;

import chess.ChessBoard;
import chess.ChessGame;
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

            // Check if the user is already in the game (either slot)
            if ((game.getWhiteUsername() != null && game.getWhiteUsername().equals(auth.getUsername()))
                    || (game.getBlackUsername() != null && game.getBlackUsername().equals(auth.getUsername()))) {
                // throw new IllegalArgumentException("User already joined this game.");
            }

            if (joinAsWhite) {
                if (game.getWhiteUsername() != null && !game.getWhiteUsername().equals(auth.getUsername())) {
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

    public ChessBoard getGameState(int gameID) {
        try {
            ChessBoard gameBoard = gameDAO.getGame(gameID).getGame().getBoard();
            if (gameBoard == null) {
                throw new IllegalArgumentException("Game not found.");
            }
            return gameBoard;
        } catch (DataAccessException e) {
            throw new RuntimeException("Error fetching game state: " + e.getMessage(), e);
        }
    }

    public void leaveGame(String authToken, int gameID) {
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
            // Remove player from the appropriate slot if they are in the game.
            if (game.getWhiteUsername() != null && game.getWhiteUsername().equals(auth.getUsername())) {
                // Remove from white slot.
                game = new GameData(game.getGameID(), null, game.getBlackUsername(), game.getGameName(), game.getGame());
            } else if (game.getBlackUsername() != null && game.getBlackUsername().equals(auth.getUsername())) {
                // Remove from black slot.
                game = new GameData(game.getGameID(), game.getWhiteUsername(), null, game.getGameName(), game.getGame());
            } else {
                throw new IllegalArgumentException("Player is not part of this game.");
            }
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating game: " + e.getMessage(), e);
        }
    }

    public void resignGame(String authToken, int gameID) {
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
            // Check that the player is actually in the game.
            if (!auth.getUsername().equals(game.getWhiteUsername()) &&
                    !auth.getUsername().equals(game.getBlackUsername())) {
                throw new IllegalArgumentException("Player is not part of this game.");
            }
            // Mark the game as over.
            ChessGame chessGame = game.getGame();
            chessGame.setGameOver(true);
            // Optionally, you could determine the winner here if desired.
            // For now we simply mark it as over.
            gameDAO.updateGame(new GameData(
                    game.getGameID(),
                    game.getWhiteUsername(),
                    game.getBlackUsername(),
                    game.getGameName(),
                    chessGame));
        } catch (DataAccessException e) {
            throw new RuntimeException("Error updating game: " + e.getMessage(), e);
        }
    }


}
package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class GameDAO {

    private final Gson gson = new Gson();

    // Create a new game and return its auto-generated gameID.
    public int createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?)";
        // Initially, both white and black usernames are null.
        // For the game state, we'll store a serialized ChessGame.
        ChessGame newGame = new ChessGame(); // assuming a default constructor exists
        String gameState = gson.toJson(newGame);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, null);
            stmt.setString(2, null);
            stmt.setString(3, gameName);
            stmt.setString(4, gameState);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new DataAccessException("No game ID generated.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    // Retrieve a game by gameID.
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM game WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameState = rs.getString("gameState");
                    // Deserialize the JSON string back into a ChessGame object.
                    ChessGame chessGame = gson.fromJson(gameState, ChessGame.class);
                    return new GameData(id, whiteUsername, blackUsername, gameName, chessGame);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

    // List all games, returning a Map of gameID to GameData.
    public Map<Integer, GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM game";
        Map<Integer, GameData> games = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                String gameState = rs.getString("gameState");
                ChessGame chessGame = gson.fromJson(gameState, ChessGame.class);
                games.put(id, new GameData(id, whiteUsername, blackUsername, gameName, chessGame));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
    }

    // Update an existing game record.
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ? WHERE gameID = ?";
        // Serialize the ChessGame object to JSON.
        String gameState = gson.toJson(game.getGame());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getWhiteUsername());
            stmt.setString(2, game.getBlackUsername());
            stmt.setString(3, game.getGameName());
            stmt.setString(4, gameState);
            stmt.setInt(5, game.getGameID());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("No game updated, game not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    // Clear all games.
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }
}

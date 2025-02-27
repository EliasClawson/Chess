package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.UUID;

public class AuthDAO {

    // Create a new auth token and store it in the database.
    public String createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return token;
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token: " + e.getMessage());
        }
    }

    // Retrieve auth data by token.
    public AuthData getAuth(String token) throws DataAccessException {
        String sql = "SELECT * FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new AuthData(token, username);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token: " + e.getMessage());
        }
    }

    // Delete an auth token (i.e., log out).
    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token: " + e.getMessage());
        }
    }

    // Clear all auth tokens.
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth tokens: " + e.getMessage());
        }
    }
}

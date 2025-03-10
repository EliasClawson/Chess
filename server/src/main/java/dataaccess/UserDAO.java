package dataaccess;

import model.UserData;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    // Create a new user in the database
    public void createUser(String username, String password, String email) throws DataAccessException {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            throw new DataAccessException("Error creating user: Missing fields");
        }

        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    // Retrieve a user by username from the database
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching user: " + e.getMessage());
        }
    }

    // Check if a user exists in the database
    public boolean userExists(String username) throws DataAccessException {
        return getUser(username) != null;
    }

    // Clear all users from the database
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM user";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}

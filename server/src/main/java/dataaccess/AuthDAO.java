package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthDAO {
    // Map to store all auth tokens and their corresponding auth data
    private final Map<String, AuthData> authTokens = new HashMap<>();

    // Create a new auth token
    public String createAuth(String username) {
        String token = UUID.randomUUID().toString();
        authTokens.put(token, new AuthData(token, username));
        return token;
    }

    // Retrieve auth data by token
    public AuthData getAuth(String token) {
        return authTokens.get(token);
    }

    // Delete an auth token (logout)
    public void deleteAuth(String token) {
        authTokens.remove(token);
    }

    // Clear all auth tokens (Just for wiping the database)
    public void clear() {
        authTokens.clear();
    }
}
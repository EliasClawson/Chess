package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    // Create a new user
    public void createUser(String username, String password, String email) {
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("User already exists.");
        }
        users.put(username, new UserData(username, password, email));
    }

    // Retrieve a user by username
    public UserData getUser(String username) {
        return users.get(username);
    }

    // Check if a user exists
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
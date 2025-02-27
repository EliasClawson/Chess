package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    // Register a new user
    public String registerUser(String username, String password, String email) {
        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Bad request: Missing fields.");
        }

        try {
            if (userDAO.userExists(username)) {
                throw new IllegalArgumentException("Username already taken.");
            }
            userDAO.createUser(username, password, email);
            return authDAO.createAuth(username);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error registering user: " + e.getMessage(), e);
        }
    }

    // Login user
    public String loginUser(String username, String password) {
        try {
            UserData user = userDAO.getUser(username);
            if (user == null) {
                System.out.println("User not found: " + username);
                throw new IllegalArgumentException("Invalid username or password.");
            }
            System.out.println("Stored hash for " + username + ": " + user.getPassword());
            boolean valid = BCrypt.checkpw(password, user.getPassword());
            System.out.println("BCrypt.checkpw result: " + valid);
            if (!valid) {
                throw new IllegalArgumentException("Invalid username or password.");
            }
            return authDAO.createAuth(username);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error logging in: " + e.getMessage(), e);
        }
    }


    // Logout user
    public void logoutUser(String authToken) {
        try {
            if (authDAO.getAuth(authToken) == null) {
                throw new IllegalArgumentException("Invalid auth token.");
            }
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error logging out: " + e.getMessage(), e);
        }
    }
}

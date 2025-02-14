package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.UserData;

// Service for handling user-related requests from users
public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    // Register a new user
    public String registerUser(String username, String password, String email) {
        // Validate inputs: if any field is null or empty, it's a bad request.
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Bad request: Missing fields."); // If missed a field, stop
        }

        if (userDAO.userExists(username)) {
            throw new IllegalArgumentException("Username already taken."); // If username taken, stop (bad)
        }

        userDAO.createUser(username, password, email);
        return authDAO.createAuth(username); // Return auth token
    }


    // Login user
    public String loginUser(String username, String password) {
        UserData user = userDAO.getUser(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return authDAO.createAuth(username); // Generate and return new auth token
    }

    // Logout user
    public void logoutUser(String authToken) {
        if (authDAO.getAuth(authToken) == null) {
            throw new IllegalArgumentException("Invalid auth token."); // You didn't say the magic word
        }
        authDAO.deleteAuth(authToken);
    }
}
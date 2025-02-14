package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class ClearService {
    private final UserDAO userDAOThingy;
    private final GameDAO gameDAOThingy;
    private final AuthDAO authDAOThingy;

    // Initialize DAOs
    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAOThingy = userDAO;
        this.gameDAOThingy = gameDAO;
        this.authDAOThingy = authDAO;
    }

    // Clear all users, games, and auth tokens, breaks without the Exception for some reason
    public void clear() throws Exception {
        userDAOThingy.clear();
        gameDAOThingy.clear();
        authDAOThingy.clear();
    }
}
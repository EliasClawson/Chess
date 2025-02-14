package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class ClearService {
    private final UserDAO userDAO_Thingy;
    private final GameDAO gameDAO_Thingy;
    private final AuthDAO authDAO_Thingy;

    // Initialize DAOs
    public ClearService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO_Thingy = userDAO;
        this.gameDAO_Thingy = gameDAO;
        this.authDAO_Thingy = authDAO;
    }

    // Clear all users, games, and auth tokens, breaks without the Exception for some reason
    public void clear() throws Exception {
        userDAO_Thingy.clear();
        gameDAO_Thingy.clear();
        authDAO_Thingy.clear();
    }
}
package server;

import spark.Spark;
import service.UserService;
import service.GameService;
import service.ClearService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

// This is the main class for the server. Main just runs this one, cause that makes sens.
// It initializes the DAOs and services, and registers the endpoints for access
public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Initialize DAOs
        UserDAO userDAOThingy = new UserDAO();
        GameDAO gameDAOThingy = new GameDAO();
        AuthDAO authDAOThingy = new AuthDAO();

        // Initialize services
        UserService userService = new UserService(userDAOThingy, authDAOThingy);
        GameService gameService = new GameService(gameDAOThingy, authDAOThingy);
        ClearService clearService = new ClearService(userDAOThingy, gameDAOThingy, authDAOThingy);

        // Register Endpoints
        Spark.delete("/db", (req, res) -> new ClearHandler(clearService).handleRequest(req, res));
        Spark.post("/user", (req, res) -> new UserHandler(userService).handleRegister(req, res));
        Spark.post("/session", (req, res) -> new UserHandler(userService).handleLogin(req, res));
        Spark.delete("/session", (req, res) -> new UserHandler(userService).handleLogout(req, res));
        Spark.get("/game", (req, res) -> new GameHandler(gameService).handleListGames(req, res));
        Spark.post("/game", (req, res) -> new GameHandler(gameService).handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> new GameHandler(gameService).handleJoinGame(req, res));

        Spark.awaitInitialization();
        return Spark.port();
    }

    // Just stop
    public void stop() {
        Spark.stop();
    }
}

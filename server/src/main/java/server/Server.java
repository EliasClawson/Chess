package server;

import spark.Spark;
import service.UserService;
import service.GameService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class Server {
    public int run(int desiredPort) {
        // Set the port for the server
        Spark.port(desiredPort);

        // Serve static files from "web" (ensure this matches your resource folder)
        Spark.staticFiles.location("web");

        // Initialize DAOs
        UserDAO userDAO = new UserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthDAO authDAO = new AuthDAO();

        // Initialize service classes
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        // Register Endpoints

        // Clear database endpoint
        Spark.delete("/db", (req, res) -> new ClearHandler().handleRequest(req, res));

        // User endpoints
        Spark.post("/user", (req, res) -> new UserHandler(userService).handleRegister(req, res));
        Spark.post("/session", (req, res) -> new UserHandler(userService).handleLogin(req, res));
        Spark.delete("/session", (req, res) -> new UserHandler(userService).handleLogout(req, res));

        // Game endpoints
        Spark.get("/game", (req, res) -> new GameHandler(gameService).handleListGames(req, res));
        Spark.post("/game", (req, res) -> new GameHandler(gameService).handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> new GameHandler(gameService).handleJoinGame(req, res));

        // Wait until the server is initialized
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}
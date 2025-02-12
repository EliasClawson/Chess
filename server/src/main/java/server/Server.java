package server;

import spark.Spark;
import service.UserService;
import service.GameService;
import service.ClearService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Initialize DAOs
        UserDAO userDAO = new UserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthDAO authDAO = new AuthDAO();

        // Initialize services
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

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

    public void stop() {
        Spark.stop();
    }
}

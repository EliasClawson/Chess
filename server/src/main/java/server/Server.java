package server;

import spark.Spark;
import service.UserService;
import service.GameService;
import service.ClearService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

public class Server {
    public int run(int desiredPort) {
        // Stop Spark if it is already running to ensure we start fresh.
        Spark.stop();
        // Wait a moment for Spark to fully shut down before reconfiguring.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore interruption during shutdown wait.
        }

        // Now we can safely configure Spark.
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // 1) Create the database if it doesn't exist.
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            e.printStackTrace();
            // Possibly exit if the DB can't be created.
        }

        // 2) Create the necessary tables if they don't exist.
        try (var conn = DatabaseManager.getConnection()) {
            String createUserTable = """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
            """;
            try (var stmt = conn.createStatement()) {
                stmt.executeUpdate(createUserTable);
            }

            String createGameTable = """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255),
                    gameState TEXT
                )
            """;
            try (var stmt = conn.createStatement()) {
                stmt.executeUpdate(createGameTable);
            }

            String createAuthTable = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
            """;
            try (var stmt = conn.createStatement()) {
                stmt.executeUpdate(createAuthTable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3) Initialize DAOs.
        UserDAO userDAOThingy = new UserDAO();
        GameDAO gameDAOThingy = new GameDAO();
        AuthDAO authDAOThingy = new AuthDAO();

        // 4) Create services.
        UserService userService = new UserService(userDAOThingy, authDAOThingy);
        GameService gameService = new GameService(gameDAOThingy, authDAOThingy);
        ClearService clearService = new ClearService(userDAOThingy, gameDAOThingy, authDAOThingy);

        // 4.5) Clear the database to remove any leftover data.
        try {
            clearService.clear();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error if needed.
        }

        // 5) Register endpoints.
        Spark.delete("/db", (req, res) -> new ClearHandler(clearService).handleRequest(req, res));
        Spark.post("/user", (req, res) -> new UserHandler(userService).handleRegister(req, res));
        Spark.post("/session", (req, res) -> new UserHandler(userService).handleLogin(req, res));
        Spark.delete("/session", (req, res) -> new UserHandler(userService).handleLogout(req, res));
        Spark.get("/game", (req, res) -> new GameHandler(gameService).handleListGames(req, res));
        Spark.post("/game", (req, res) -> new GameHandler(gameService).handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> new GameHandler(gameService).handleJoinGame(req, res));

        Spark.awaitInitialization();
        System.out.println("Server started on port " + Spark.port());
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}

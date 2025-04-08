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

//import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import server.websocket.GameWebSocket;


public class Server {
    public int run(int desiredPort) {
        // Stop Spark if it is already running
        Spark.stop();
        // Wait a moment for Spark to fully shut down before reconfiguring - breaks otherwise
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Do nothing I guess?
        }

        // Configure Spark
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Steps from chatGPT
        // 1) Create the database if it doesn't exist.
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            e.printStackTrace();
            // Something went terribly wrong
        }

        // 1.5) Clear all data
        // ACTUALLY DONT! THIS BROKE THE TESTS (Persistence tests)

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
            // Something went terribly wrong
        }

        // 3) Initialize DAOs.
        UserDAO userDAOThingy = new UserDAO();
        GameDAO gameDAOThingy = new GameDAO();
        AuthDAO authDAOThingy = new AuthDAO();

        // 4) Create services.
        UserService userService = new UserService(userDAOThingy, authDAOThingy);
        GameService gameService = new GameService(gameDAOThingy, authDAOThingy);
        ClearService clearService = new ClearService(userDAOThingy, gameDAOThingy, authDAOThingy);

        // This one also broke the persistence test so it's bad
        // Had to erase this to pass the code quality test

        // 5) Register endpoints. (Hey spark the endpoints are here!)
        Spark.delete("/db", (req, res) -> new ClearHandler(clearService).handleRequest(req, res));
        Spark.post("/user", (req, res) -> new UserHandler(userService).handleRegister(req, res));
        Spark.post("/session", (req, res) -> new UserHandler(userService).handleLogin(req, res));
        Spark.delete("/session", (req, res) -> new UserHandler(userService).handleLogout(req, res));
        Spark.get("/game", (req, res) -> new GameHandler(gameService).handleListGames(req, res));
        Spark.post("/game", (req, res) -> new GameHandler(gameService).handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> new GameHandler(gameService).handleJoinGame(req, res));
        Spark.get("/game/state", (req, res) -> new GameHandler(gameService).handleGetGameState(req, res));
        Spark.delete("/game/leave", (req, res) -> new GameHandler(gameService).handleLeaveGame(req, res));
        Spark.delete("/game/resign", (req, res) -> new GameHandler(gameService).handleResignGame(req, res));
        Spark.get("/game/fullstate", (req, res) -> new GameHandler(gameService).handleGetFullGameState(req, res));

        Spark.awaitInitialization();

        // Start Jetty WebSocket server on a separate port (or same if not conflicting)
        new Thread(() -> {
            try {
                int websocketPort = 8081; // Change to 8080 if you want to share the port
                org.eclipse.jetty.server.Server wsServer = new org.eclipse.jetty.server.Server(websocketPort);

                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");

                WebSocketHandler wsHandler = new WebSocketHandler() {
                    @Override
                    public void configure(WebSocketServletFactory factory) {
                        factory.register(GameWebSocket.class);
                    }
                };

                context.setHandler(wsHandler);
                wsServer.setHandler(context);

                wsServer.start();
                System.out.println("WebSocket server started on port " + websocketPort);
                wsServer.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


        System.out.println("Server started on port " + Spark.port()); // Server is definitely running...
        return Spark.port();
    }

    public void stop() {
        // Stop the server?
        Spark.stop();
    }
}

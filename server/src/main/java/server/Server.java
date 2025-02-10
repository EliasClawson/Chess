package server;

import com.sun.net.httpserver.HttpServer;
import service.UserService;
import service.GameService;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Server {
    private HttpServer server;

    public void run(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Initialize DAOs and Services
            UserDAO userDAO = new UserDAO();
            GameDAO gameDAO = new GameDAO();
            AuthDAO authDAO = new AuthDAO();

            UserService userService = new UserService(userDAO, authDAO);
            GameService gameService = new GameService(gameDAO, authDAO);

            // Register endpoints
            server.createContext("/user", new UserHandler(userService));
            server.createContext("/session", new UserHandler(userService)); // login/logout
            server.createContext("/game", new GameHandler(gameService));
            server.createContext("/game/join", new GameHandler(gameService));
            // Debug context to catch any unmatched requests
            server.createContext("/", new DebugHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("♕ 240 Chess Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}

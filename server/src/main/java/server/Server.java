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

            // Initialize DAOs
            UserDAO userDAO = new UserDAO();
            GameDAO gameDAO = new GameDAO();
            AuthDAO authDAO = new AuthDAO();

            // Creates an instance of UserService to handle user requests
            UserService userService = new UserService(userDAO, authDAO);
            // Creates an instance of gameService to handle game requests
            GameService gameService = new GameService(gameDAO, authDAO);

            // Register endpoints with userService and gameService, and pass in userService/gameService
            server.createContext("/user", new UserHandler(userService));
            server.createContext("/session", new UserHandler(userService)); // login/logout
            server.createContext("/game", new GameHandler(gameService));
            server.createContext("/game/join", new GameHandler(gameService));
            // Debugger created to catch other requests that I don't understand
            server.createContext("/", new DebugHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("♕ 240 Chess Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Error settup up server and endpoints: \n" + e.getMessage() + "\n");
        }
    }
}

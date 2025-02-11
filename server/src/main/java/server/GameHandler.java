package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import model.GameData;
import service.GameService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import java.util.Map;

public class GameHandler implements HttpHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // Handles incoming HTTP requests for user-related operations
    @Override // Makes sure I don't screw shit up
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Send the request to the appropriate handler
        if ("POST".equalsIgnoreCase(method)) {
            if (path.equals("/game")) {
                handleCreateGame(exchange);
            } else if (path.equals("/game/join")) {
                handleJoinGame(exchange);
            }
        } else if ("GET".equalsIgnoreCase(method) && path.equals("/game")) {
            handleListGames(exchange);
        } else {
            sendResponse(exchange, 404, "Endpoint not found");
        }
    }

    // Create Game requested, respond to client
    private void handleCreateGame(HttpExchange exchange) throws IOException {
        String authToken = exchange.getRequestHeaders().getFirst("Authorization");
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        CreateGameRequest request = gson.fromJson(requestBody, CreateGameRequest.class);

        try {
            int gameId = gameService.createGame(authToken, request.gameName);
            sendResponse(exchange, 200, gson.toJson(new CreateGameResponse(gameId)));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 401, e.getMessage());
        }
    }

    // Join Game requested, respond to client
    private void handleJoinGame(HttpExchange exchange) throws IOException {
        String authToken = exchange.getRequestHeaders().getFirst("Authorization");
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        JoinGameRequest request = gson.fromJson(requestBody, JoinGameRequest.class);

        try {
            gameService.joinGame(authToken, request.gameID, request.color.equalsIgnoreCase("white"));
            sendResponse(exchange, 200, "{}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 403, e.getMessage());
        }
    }

    // List Games requested, respond to client
    private void handleListGames(HttpExchange exchange) throws IOException {
        try {
            Map<Integer, GameData> games = gameService.listGames();
            sendResponse(exchange, 200, gson.toJson(games));
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    // Helper function to send a response to client
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static class CreateGameRequest {
        String gameName;
    }

    private static class CreateGameResponse {
        int gameID;

        CreateGameResponse(int gameID) { // Constructor to make life easier
            this.gameID = gameID;
        }
    }

    private static class JoinGameRequest {
        int gameID;
        String color;
    }
}
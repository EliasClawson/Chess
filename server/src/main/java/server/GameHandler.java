package server;

import com.google.gson.Gson;
import model.*;
import spark.Request;
import spark.Response;
import service.GameService;
import java.util.List;
import java.util.stream.Collectors;

public class GameHandler {
    private final GameService gameService; // From the file GameService
    private final Gson gson = new Gson(); // Converts JSON to Java objects (?)

    // Constructor
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // Handles GET /game: lists all games.
    public Object handleListGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            List<GameData> gameDataList = gameService.listGames(authToken);
            List<GameInfo> games = gameDataList.stream()
                    .map(g -> new GameInfo(g.getGameID(), g.getWhiteUsername(), g.getBlackUsername(), g.getGameName()))
                    .collect(Collectors.toList());

            res.status(200);
            return gson.toJson(new ListGamesResponse(games));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    // Handles POST /game: creates a new game.
    public Object handleCreateGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            int gameID = gameService.createGame(authToken, createGameRequest.gameName());
            res.status(200);
            return gson.toJson(new CreateGameResponse(gameID));
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            // Check if the error message indicates an invalid auth token.
            if (errorMessage != null && errorMessage.contains("Invalid auth token")) {
                res.status(401);
            } else {
                res.status(400);
            }
            return gson.toJson(new ErrorResponse("Error: " + errorMessage));
        }
    }


    // Handles PUT /game: allows a user to join a game.
    public Object handleJoinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);

            // Validate the team color
            String color = joinGameRequest.playerColor();
            if (color == null || color.trim().isEmpty() ||
                    (!color.equalsIgnoreCase("WHITE") && !color.equalsIgnoreCase("BLACK"))) {
                throw new IllegalArgumentException("Invalid team color.");
            }

            // Convert to boolean: true if WHITE, false if BLACK
            boolean joinAsWhite = color.equalsIgnoreCase("WHITE");
            gameService.joinGame(authToken, joinGameRequest.gameID(), joinAsWhite);

            res.status(200);
            return gson.toJson(new Object()); // returns "{}"
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            // Check for specific error cases to set the proper status code.
            if (errorMessage != null && errorMessage.contains("Invalid auth token")) {
                res.status(401);
            } else if (errorMessage != null && errorMessage.contains("slot already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return gson.toJson(new ErrorResponse("Error: " + errorMessage));
        }
    }
}
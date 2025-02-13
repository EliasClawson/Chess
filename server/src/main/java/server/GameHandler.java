package server;

import com.google.gson.Gson;
import model.GameData;
import spark.Request;
import spark.Response;
import service.GameService;
import java.util.List;
import java.util.stream.Collectors;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

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
            // Convert the string "WHITE" or "BLACK" to a boolean value.
            // For example, assume white if the string equals "WHITE" (ignoring case).
            boolean joinAsWhite = joinGameRequest.playerColor().equalsIgnoreCase("WHITE");
            gameService.joinGame(authToken, joinGameRequest.gameID(), joinAsWhite);
            res.status(200);
            return gson.toJson(new Object()); // returns "{}"
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

}
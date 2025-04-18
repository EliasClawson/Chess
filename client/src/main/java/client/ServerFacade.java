package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.stream.Collectors;

import chess.ChessBoard;
import com.google.gson.Gson;

import com.google.gson.Gson;
import model.*;
import model.LoginRequest;
import model.LoginResponse;
import model.RegisterRequest;
import model.RegisterResponse;
import model.CreateGameRequest;
import model.CreateGameResponse;
import model.ListGamesResponse;
import model.JoinGameRequest;
import model.GameInfo;
import chess.ChessGame;

public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port; // Just sets up the base URL
    }

    // Helper method to check the HTTP response.
    // If the response code is 40x, we throw an IllegalArgumentException with the error message.
    // If it's 50x (or any non-20x outside 40x), we throw an IOException.
    private void checkResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            return;
        }
        InputStream is = connection.getErrorStream();
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line.trim());
            }
        }
        if (responseCode >= 400 && responseCode < 500) {
            throw new IllegalArgumentException(errorResponse.toString());
        } else {
            throw new IOException("Server returned error: " + responseCode + " " + errorResponse.toString());
        }
    }

    public AuthData register(String username, String password, String email) throws IOException {
        String endpoint = baseUrl + "/user";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            RegisterRequest reqObj = new RegisterRequest(username, password, email);
            String jsonInputString = gson.toJson(reqObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            checkResponse(connection);  // Throws appropriate exception if not successful

            // Read successful response.
            InputStream is = connection.getInputStream();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            RegisterResponse regResp = gson.fromJson(response.toString(), RegisterResponse.class);
            return new AuthData(regResp.authToken(), regResp.username());
        } finally {
            connection.disconnect();
        }
    }

    public AuthData login(String username, String password) throws IOException {
        String endpoint = baseUrl + "/session";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            LoginRequest reqObj = new LoginRequest(username, password);
            String jsonInputString = gson.toJson(reqObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            checkResponse(connection);

            InputStream is = connection.getInputStream();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            LoginResponse loginResp = gson.fromJson(response.toString(), LoginResponse.class);
            return new AuthData(loginResp.authToken(), loginResp.username());
        } finally {
            connection.disconnect();
        }
    }

    public void logout(String authToken) throws IOException {
        String endpoint = baseUrl + "/session";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", authToken);

            checkResponse(connection);
            // Successful logout; nothing to return.
        } finally {
            connection.disconnect();
        }
    }

    public List<GameInfo> listGames(String authToken) throws IOException {
        String endpoint = baseUrl + "/game";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", authToken);

            checkResponse(connection);

            InputStream is = connection.getInputStream();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }
            ListGamesResponse listResp = gson.fromJson(response.toString(), ListGamesResponse.class);
            return listResp.games();
        } finally {
            connection.disconnect();
        }
    }

    public int createGame(String authToken, String gameName) throws IOException {
        String endpoint = baseUrl + "/game";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authToken);

            CreateGameRequest reqObj = new CreateGameRequest(gameName);
            String jsonInputString = gson.toJson(reqObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            checkResponse(connection);

            InputStream is = connection.getInputStream();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }
            CreateGameResponse respObj = gson.fromJson(response.toString(), CreateGameResponse.class);
            return respObj.gameID();
        } finally {
            connection.disconnect();
        }
    }

    public void joinGame(String authToken, int gameID, boolean joinAsWhite) throws IOException {
        String endpoint = baseUrl + "/game";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authToken);

            String playerColor = joinAsWhite ? "WHITE" : "BLACK";
            JoinGameRequest reqObj = new JoinGameRequest(playerColor, gameID);
            String jsonInputString = gson.toJson(reqObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            checkResponse(connection);
            // Successful join returns an empty JSON; nothing further to do.
        } finally {
            connection.disconnect();
        }
    }

    public ChessBoard getGameState(String authToken, int gameID) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        // Build a URI for the endpoint (adjust path as needed)
        URI uri = URI.create(baseUrl + "/game/state?gameID=" + gameID);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Authorization", authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Assuming ChessGame is a shared model representing the game state.
            ChessBoard gameBoard = new Gson().fromJson(response.body(), ChessBoard.class);
            return gameBoard;
        } else {
            throw new Exception("Error fetching game state: " + response.body());
        }
    }

    public ChessGame getFullGameState(String authToken, int gameID) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        // Build a URI for the endpoint (adjust path as needed)
        URI uri = URI.create(baseUrl + "/game/fullstate?gameID=" + gameID);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Authorization", authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("In Server facade, response is " + response);
            // Assuming ChessGame is a shared model representing the game state.
            ChessGame game = new Gson().fromJson(response.body(), ChessGame.class);

            System.out.println("In Server facade, turn is " + game.getTeamTurn());
            return game;
        } else {
            throw new Exception("Error fetching game state: " + response.body());
        }
    }

    public void leaveGame(String authToken, int gameID) throws IOException {
        String endpoint = baseUrl + "/game/leave?gameID=" + gameID;
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);
        // Use checkResponse() already implemented in your facade to validate the response.
        checkResponse(connection);
        connection.disconnect();
    }

    public void resignGame(String authToken, int gameID) throws Exception {
        String endpoint = baseUrl + "/game/resign?gameID=" + gameID;
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", authToken);
            connection.connect();
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                InputStream errorStream = connection.getErrorStream();
                String errorMsg = new BufferedReader(new InputStreamReader(errorStream))
                        .lines().collect(Collectors.joining("\n"));
                throw new Exception("Error resigning game: " + errorMsg);
            }
        } finally {
            connection.disconnect();
        }
    }



    // This one is just for testing.
    public void clearDatabase() throws IOException {
        String endpoint = baseUrl + "/db";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("DELETE");
            // No additional headers should be necessary for clearing the DB.
            checkResponse(connection);
            // If the response code is 2xx, the DB was cleared successfully.
        } finally {
            connection.disconnect();
        }
    }
}

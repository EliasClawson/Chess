package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import model.AuthData;
import server.*;

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

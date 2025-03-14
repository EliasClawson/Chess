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
import model.AuthData;

public class ServerFacade {

    private final String baseUrl;

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    //Registers a new user by sending an HTTP POST to the server's registration endpoint.
    public AuthData register(String username, String password, String email) throws IOException {
        String endpoint = baseUrl + "/user";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Create a RegisterRequest record
            RegisterRequest reqObj = new RegisterRequest(username, password, email);
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(reqObj);

            // Write the JSON payload to the request body.
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8"); // Must use UTF-8 encoding apparently
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
                // Parse the JSON response into a RegisterResponse record.
                RegisterResponse regResp = gson.fromJson(response.toString(), RegisterResponse.class);
                // Convert the RegisterResponse into your shared AuthData.
                return new AuthData(regResp.authToken(), regResp.username());
            } else {
                throw new IOException("Server returned error: " + responseCode + " " + response.toString());
            }
        } finally {
            connection.disconnect();
        }
    }

    // Logs in the user by sending POST request to /session
    public AuthData login(String username, String password) throws IOException {
        String endpoint = baseUrl + "/session";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Create a LoginRequest using your server's record type.
            LoginRequest reqObj = new LoginRequest(username, password);
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(reqObj);

            // Write the JSON payload to the request body.
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
                // Parse the JSON response into a LoginResponse record.
                LoginResponse loginResp = gson.fromJson(response.toString(), LoginResponse.class);
                // Create and return an AuthData object using the data from loginResp.
                return new AuthData(loginResp.authToken(), loginResp.username());
            } else {
                throw new IOException("Server returned error: " + responseCode + " " + response.toString());
            }
        } finally {
            connection.disconnect();
        }
    }

    // Logs out the user by sending DELETE request to /session
    public void logout(String authToken) throws IOException {
        String endpoint = baseUrl + "/session";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("DELETE");
            // Set the Authorization header as expected by the server handler.
            connection.setRequestProperty("Authorization", authToken);

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                // If there's an error, read the error stream and throw an exception.
                InputStream is = connection.getErrorStream();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                }
                throw new IOException("Server returned error: " + responseCode + " " + response.toString());
            }
            // Success: the server returns an empty JSON "{}" and a 200 status code.
        } finally {
            connection.disconnect();
        }
    }

    // Creates a new game by sending a POST request to /game.
    public int createGame(String authToken, String gameName) throws IOException {
        String endpoint = baseUrl + "/game";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authToken);

            // Create the request payload using CreateGameRequest.
            CreateGameRequest reqObj = new CreateGameRequest(gameName);
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(reqObj);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            if (responseCode >= 200 && responseCode < 300) {
                // Parse the response into a CreateGameResponse.
                CreateGameResponse respObj = gson.fromJson(response.toString(), CreateGameResponse.class);
                return respObj.gameID(); // assuming the record has a gameID() accessor
            } else {
                throw new IOException("Server returned error: " + responseCode + " " + response.toString());
            }
        } finally {
            connection.disconnect();
        }
    }


}
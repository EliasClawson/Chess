package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import service.UserService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;

public class UserHandler implements HttpHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method)) {
            if (path.equals("/user")) { // Register
                handleRegister(exchange);
            } else if (path.equals("/session")) { // Login
                handleLogin(exchange);
            }
        } else if ("DELETE".equalsIgnoreCase(method) && path.equals("/session")) { // Logout
            handleLogout(exchange);
        } else {
            sendResponse(exchange, 404, "Endpoint not found");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        RegisterRequest request = gson.fromJson(requestBody, RegisterRequest.class);

        try {
            String authToken = userService.registerUser(request.username, request.password, request.email);
            sendResponse(exchange, 200, gson.toJson(new RegisterResponse(authToken)));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        LoginRequest request = gson.fromJson(requestBody, LoginRequest.class);

        try {
            String authToken = userService.loginUser(request.username, request.password);
            sendResponse(exchange, 200, gson.toJson(new LoginResponse(authToken)));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 401, e.getMessage());
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String authToken = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            userService.logoutUser(authToken);
            sendResponse(exchange, 200, "{}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 401, e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static class RegisterRequest {
        String username;
        String password;
        String email;
    }

    private static class RegisterResponse {
        String authToken;

        RegisterResponse(String authToken) {
            this.authToken = authToken;
        }
    }

    private static class LoginRequest {
        String username;
        String password;
    }

    private static class LoginResponse {
        String authToken;

        LoginResponse(String authToken) {
            this.authToken = authToken;
        }
    }
}
// UserHandler.java
package server;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import service.UserService;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object handleRegister(Request req, Response res) {
        try {
            RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
            String authToken = userService.registerUser(
                    registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email()
            );
            res.status(200);
            return gson.toJson(new RegisterResponse(authToken, registerRequest.username()));
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("already taken")) {
                res.status(403);
            } else {
                res.status(400);
            }
            return gson.toJson(new ErrorResponse("Error: " + errorMessage));
        }
    }


    public Object handleLogin(Request req, Response res) {
        try {
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            String authToken = userService.loginUser(loginRequest.username(), loginRequest.password());
            res.status(200);
            return gson.toJson(new LoginResponse(authToken, loginRequest.username()));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    public Object handleLogout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            userService.logoutUser(authToken);
            res.status(200);
            return gson.toJson(new Object()); // returns "{}"
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
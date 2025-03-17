// UserHandler.java
package server;

import com.google.gson.Gson;
import model.LoginRequest;
import model.LoginResponse;
import model.RegisterRequest;
import model.RegisterResponse;
import spark.Request;
import spark.Response;
import service.UserService;

// WORKING VERSION, DON't CHANGE (for now)
public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object handleRegister(Request req, Response res) {
        System.out.println("Register request body: " + req.body() + "\n");
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
        System.out.println("Login request body: " + req.body() + "\n");
        try {
            System.out.println("Login request body: " + req.body());
            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
            System.out.println("Deserialized loginRequest: username=" + loginRequest.username()
                    + ", password=" + loginRequest.password());
            String authToken = userService.loginUser(loginRequest.username(), loginRequest.password());
            res.status(200);
            return gson.toJson(new LoginResponse(authToken, loginRequest.username()));
        } catch (Exception e) {
            e.printStackTrace();
            // Something went terribly wrong
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }


    public Object handleLogout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            userService.logoutUser(authToken);
            res.status(200);
            return gson.toJson(new Object()); // returns empty Json "{}"
        } catch (Exception e) {
            res.status(401);
            // Something went terribly wrong
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
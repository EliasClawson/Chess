package model;

public class AuthData {
    private String authToken;
    private String username;

    public AuthData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }

    public String getAuthToken() {
        System.out.println("Auth Token: " + authToken);
        return authToken;
    }
    public String getUsername() {
        System.out.println("Username: " + username);
        return username;
    }
}
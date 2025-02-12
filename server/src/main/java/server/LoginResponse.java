package server;

public record LoginResponse(String authToken, String username) { }
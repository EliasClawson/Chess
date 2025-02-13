package server;

// Color is a string: "WHITE" or "BLACK" (not a boolean you fool)
public record JoinGameRequest(String playerColor, int gameID) { }

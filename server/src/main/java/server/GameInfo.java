package server;

// So apparerntly, this can just store all top-level game data.
public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) { }
package model;

import chess.ChessGame;

public class GameData {
    private int gameID; // Why are these all yellow???
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

    // Getters
    public int getGameID() {
        System.out.println("getGameID() called for game with ID: " + gameID);
        return gameID;
    }
    public String getWhiteUsername() {
        System.out.println("getWhiteUsername() called for game with ID: " + gameID);
        return whiteUsername;
    }
    public String getBlackUsername() {
        System.out.println("getBlackUsername() called for game with ID: " + gameID);
        return blackUsername;
    }
    public String getGameName() {
        System.out.println("getGameName() called for game with ID: " + gameID);
        return gameName;
    }
    public ChessGame getGame() {
        System.out.println("getGame() called for game with ID: " + gameID);
        System.out.println("Asking for game, turn is: " + game.getTeamTurn());
        return game;
    }
}
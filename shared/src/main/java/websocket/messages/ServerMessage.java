package websocket.messages;

import chess.ChessGame;

public class ServerMessage {
    private ServerMessageType serverMessageType;
    private String payload; // new field
    private ChessGame game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String payload) {
        this.serverMessageType = type;
        this.payload = payload;
    }

    public ServerMessage(ServerMessageType type, ChessGame gameInMessage, String payload) {
        this.serverMessageType = type;
        this.game = gameInMessage;
        this.payload = payload;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getPayload() {
        return payload;
    }

    public ChessGame getGame() {
        return game;
    }
}

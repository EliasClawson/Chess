package websocket.messages;

public class ServerMessage {
    private ServerMessageType serverMessageType;
    private String payload; // new field

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String payload) {
        this.serverMessageType = type;
        this.payload = payload;
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getPayload() {
        return payload;
    }
}

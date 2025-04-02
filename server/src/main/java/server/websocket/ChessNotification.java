package webSocketMessages;

public class ChessNotification {

    public enum NotificationType {
        PLAYER_JOINED,
        MOVE_MADE,
        RESIGN,
        PLAYER_LEFT
    }

    private NotificationType type;
    private String message;

    // No-argument constructor for Gson
    public ChessNotification() {}

    public ChessNotification(NotificationType type, String message) {
        this.type = type;
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        // You could use Gson here to output JSON if desired
        return "{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}";
    }
}

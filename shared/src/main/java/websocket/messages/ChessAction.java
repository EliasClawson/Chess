package websocket.messages;

public class ChessAction {

    public enum ActionType {
        JOIN,
        MOVE,
        RESIGN,
        LEAVE
    }

    private ActionType type;
    private String username;
    private int gameId;
    private String move; // Only needed for MOVE actions

    // No-argument constructor for Gson
    public ChessAction() {}

    public ChessAction(ActionType type, String username, int gameId, String move) {
        this.type = type;
        this.username = username;
        this.gameId = gameId;
        this.move = move;
    }

    public ActionType getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public int getGameId() {
        return gameId;
    }

    public String getMove() {
        return move;
    }
}

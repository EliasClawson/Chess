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
    private String displayGameNumber;
    private String playerRole;

    // No-argument constructor for Gson
    public ChessAction() {}

    // Existing constructor (if needed)
    public ChessAction(ActionType type, String username, int gameId, String move) {
        this.type = type;
        this.username = username;
        this.gameId = gameId;
        this.move = move;
    }

    // New constructor including displayGameNumber and playerRole
    public ChessAction(ActionType type, String username, int gameId, String move, String displayGameNumber, String playerRole) {
        this.type = type;
        this.username = username;
        this.gameId = gameId;
        this.move = move;
        this.displayGameNumber = displayGameNumber;
        this.playerRole = playerRole;
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

    // New getters
    public String getDisplayNumber() {
        return displayGameNumber;
    }

    public String getPlayerRole() {
        return playerRole;
    }
}

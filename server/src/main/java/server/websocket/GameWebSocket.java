package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ChessAction;
import websocket.messages.ChessNotification;

import java.io.IOException;

@WebSocket
public class GameWebSocket {

    // Use a static (shared) connection manager so all instances share the same connection list.
    private static final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        ChessAction action = new Gson().fromJson(message, ChessAction.class);
        switch (action.getType()) {
            case JOIN:
                joinGame(action, session);
                break;
            case MOVE:
                handleMove(action, session);
                break;
            case RESIGN:
                handleResign(action, session);
                break;
            case LEAVE:
                leaveGame(action);
                break;
            default:
                // Optionally handle unknown actions.
                break;
        }
    }

    private void joinGame(ChessAction action, Session session) throws IOException {
        String username = action.getUsername();
        int gameId = action.getGameId();
        // Use the display number from the action (e.g., "5" for the 5th game) instead of the raw gameID.
        String displayNumber = action.getDisplayNumber();
        // Role should be "WHITE", "BLACK", or "OBSERVER"
        String role = action.getPlayerRole();
        // Add this connection to the shared connection manager.
        connections.add(username, gameId, session);
        String msg = username + " joined game " + displayNumber + " as " + role;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.PLAYER_JOINED, msg);
        connections.broadcast(gameId, notification);
    }

    private void handleMove(ChessAction action, Session session) throws IOException {
        String username = action.getUsername();
        int gameId = action.getGameId();
        String move = action.getMove();
        // For a move, we simply announce the move.
        String msg = username + " moved: " + move;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.MOVE_MADE, msg);
        connections.broadcast(gameId, notification);
    }

    private void handleResign(ChessAction action, Session session) throws IOException {
        String username = action.getUsername();
        int gameId = action.getGameId();
        String displayNumber = action.getDisplayNumber();
        String role = action.getPlayerRole();
        String msg = username + " resigned from game " + displayNumber + " as " + role;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.RESIGN, msg);
        connections.broadcast(gameId, notification);
    }

    private void leaveGame(ChessAction action) throws IOException {
        String username = action.getUsername();
        int gameId = action.getGameId();
        String displayNumber = action.getDisplayNumber();
        String role = action.getPlayerRole();
        String msg = username + " left game " + displayNumber + " as " + role;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.PLAYER_LEFT, msg);
        connections.broadcast(gameId, notification);
        connections.remove(username, gameId);
    }
}

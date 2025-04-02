package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.ChessAction;
import webSocketMessages.ChessNotification;

import java.io.IOException;

@WebSocket
public class GameWebSocket {

    // A connection manager that groups sessions by gameId.
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        ChessAction action = new Gson().fromJson(message, ChessAction.class);
        switch (action.getType()) {
            case JOIN:
                joinGame(action.getUsername(), action.getGameId(), session);
                break;
            case MOVE:
                handleMove(action.getUsername(), action.getGameId(), action.getMove(), session);
                break;
            case RESIGN:
                handleResign(action.getUsername(), action.getGameId(), session);
                break;
            case LEAVE:
                leaveGame(action.getUsername(), action.getGameId());
                break;
            default:
                // Unknown action – optionally send an error back.
                break;
        }
    }

    private void joinGame(String username, int gameId, Session session) throws IOException {
        // Add the session under the given game.
        connections.add(username, gameId, session);
        String msg = username + " joined game " + gameId;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.PLAYER_JOINED, msg);
        connections.broadcast(gameId, notification);
    }

    private void handleMove(String username, int gameId, String move, Session session) throws IOException {
        // TODO: Validate and process the move via your GameService
        String msg = username + " moved: " + move;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.MOVE_MADE, msg);
        connections.broadcast(gameId, notification);
    }

    private void handleResign(String username, int gameId, Session session) throws IOException {
        String msg = username + " resigned from game " + gameId;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.RESIGN, msg);
        connections.broadcast(gameId, notification);
    }

    private void leaveGame(String username, int gameId) throws IOException {
        connections.remove(username, gameId);
        String msg = username + " left game " + gameId;
        ChessNotification notification = new ChessNotification(ChessNotification.NotificationType.PLAYER_LEFT, msg);
        connections.broadcast(gameId, notification);
    }
}

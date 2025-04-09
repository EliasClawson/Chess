package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ChessAction;
import websocket.messages.ChessNotification;

import java.io.IOException;
import service.GameService;
import websocket.messages.ServerMessage;

@WebSocket
public class GameWebSocket {

    // Use a static (shared) connection manager so all instances share the same connection list.
    private static final ConnectionManager connections = new ConnectionManager();

    private final GameService gameService;

    public GameWebSocket(GameService gameService) {
        this.gameService = gameService;
    }

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
        String moveStr = action.getMove(); // e.g., "e2e4"

        try {
            // Update game state in the database via GameService
            this.gameService.makeMove(gameId, username, moveStr);

            // Get the updated game after move
            ChessGame updatedGame = this.gameService.getFullGameState(gameId);

            // ✅ Broadcast a normal notification that a move was made
            String moveMessage = username + " moved: " + moveStr;
            ChessNotification notification = new ChessNotification(
                    ChessNotification.NotificationType.MOVE_MADE,
                    moveMessage
            );
            connections.broadcast(gameId, notification);

            // ✅ Broadcast the new board state as a raw ServerMessage
            ServerMessage loadGameMessage = new ServerMessage(
                    ServerMessage.ServerMessageType.LOAD_GAME,
                    new Gson().toJson(updatedGame.getBoard())
            );
            String json = new Gson().toJson(loadGameMessage);
            connections.broadcastRaw(gameId, json);  // 🔥 this sends just the JSON, not wrapped in another object

        } catch (Exception e) {
            // Broadcast an error message (as a normal notification)
            ChessNotification errorNotification = new ChessNotification(
                    ChessNotification.NotificationType.MOVE_MADE,
                    "Error: " + e.getMessage()
            );
            connections.broadcast(gameId, errorNotification);
        }
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

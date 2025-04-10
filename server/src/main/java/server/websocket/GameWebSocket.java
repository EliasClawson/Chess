package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import websocket.messages.ChessAction;
import websocket.messages.ChessNotification;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import service.GameService;
import websocket.messages.ServerMessage;

@WebSocket
public class GameWebSocket {

    // Use a static (shared) connection manager so all instances share the same connection list.
    private static final ConnectionManager connections = new ConnectionManager();

    private final GameService gameService;

    public GameWebSocket() {
        this.gameService = new GameService(new GameDAO(), new AuthDAO());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("🧪 Raw WebSocket message: " + message);
        ChessAction action = new Gson().fromJson(message, ChessAction.class);
        System.out.println("Parsed action type: " + action.getType());
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
        ChessGame game = this.gameService.getFullGameState(gameId);
        // Use the display number from the action (e.g., "5" for the 5th game) instead of the raw gameID.
        String displayNumber = action.getDisplayNumber();
        // Role should be "WHITE", "BLACK", or "OBSERVER"
        String role = action.getPlayerRole();
        // Add this connection to the shared connection manager.
        connections.add(username, gameId, session);

        //Server Message
        String msg = "You joined game " + displayNumber + " as " + role;
        ServerMessage loadGameMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, msg);
        System.out.println("🔔 Sending LOAD_GAME message to new player: " + username);
        connections.sendTo(gameId, username, loadGameMsg);

        // Chess notification
        ChessNotification playerNotification = new ChessNotification(
                ChessNotification.NotificationType.PLAYER_JOINED,
                username + " joined game " + displayNumber + " as " + role
        );
        System.out.println("🔔 Sending PLAYER_JOINED notification everyone except: " + username);
        connections.broadcast(gameId, username, playerNotification);
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

    //@OnWebSocketError
    //private void reportError(Session session, String message) throws IOException {
    //    System.out.println("Error: " + message);
    //}
}

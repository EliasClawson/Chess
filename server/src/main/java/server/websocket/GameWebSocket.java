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
            // Update the game state (you must implement this in your GameService).
            // This call should validate the move, update the ChessGame (and database, if needed), etc.
            // It may throw an exception if the move is illegal.
            this.gameService.makeMove(gameId, username, moveStr);

            // Now get the updated game state. We assume you have a method that returns a full ChessGame.
            GameService gameService = this.gameService;

            ChessGame updatedGame = this.gameService.getFullGameState(gameId);


            // Create a server message that tells clients to load the new board.
            // Here we assume ServerMessage has a constructor that accepts a type and a payload (the board or full game state as JSON).
            String boardJson = new Gson().toJson(updatedGame.getBoard());
            ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, boardJson);
            String jsonMessage = new Gson().toJson(loadMessage);

            // Broadcast the updated board to everyone in this game.
            connections.broadcast(gameId, new ChessNotification(ChessNotification.NotificationType.MOVE_MADE, jsonMessage));
        } catch (Exception e) {
            // If the move is invalid or an error occurred, send an error notification.
            ChessNotification errorNotification = new ChessNotification(ChessNotification.NotificationType.MOVE_MADE, "Error: " + e.getMessage());
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

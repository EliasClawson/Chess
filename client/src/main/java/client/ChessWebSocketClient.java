package client;

import websocket.messages.ChessAction;
import websocket.messages.ChessNotification;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.CloseReason;
import com.google.gson.Gson;
import java.net.URI;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private final Gson gson = new Gson();
    public volatile boolean leaveAcknowledged = false;  // add this


    // New overloaded connect method that accepts extra fields.
    public void connect(String uri, String username, int gameId, String displayGameNumber, String role) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
        // It is assumed onOpen will be called soon; in production you might want to wait
        String joinMessage = gson.toJson(new ChessAction(
                ChessAction.ActionType.JOIN,
                username,
                gameId,
                null, // move is null for join
                displayGameNumber,
                role
        ));
        session.getAsyncRemote().sendText(joinMessage);
    }

    // Retain your existing methods or add similar overloaded ones for resign/leave if needed.
    public void sendResign(String username, int gameId, String displayGameNumber, String role) {
        String resignMessage = gson.toJson(new ChessAction(
                ChessAction.ActionType.RESIGN,
                username,
                gameId,
                null,
                displayGameNumber,
                role
        ));
        session.getAsyncRemote().sendText(resignMessage);
    }

    public void sendLeave(String username, int gameId, String displayGameNumber, String role) {
        String leaveMessage = gson.toJson(new ChessAction(
                ChessAction.ActionType.LEAVE,
                username,
                gameId,
                null,
                displayGameNumber,
                role
        ));
        session.getAsyncRemote().sendText(leaveMessage);

    }

    @OnOpen
    public void onOpen(Session session) {
        //System.out.println("Connected to WebSocket server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ChessNotification notification = gson.fromJson(message, ChessNotification.class);
        System.out.println("⚡ WebSocket message: " + notification.getMessage());

        // Check if this message indicates the user has left
        if (notification.getType() == ChessNotification.NotificationType.PLAYER_LEFT &&
                notification.getMessage().contains("left game")) {
            leaveAcknowledged = true;
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Successfully disconnected from game server");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error:");
        throwable.printStackTrace();
    }

    public void close() throws Exception {
        if (session != null) session.close();
    }
}

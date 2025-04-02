package client;

import jakarta.websocket.*;
import com.google.gson.Gson;

import java.net.URI;

@ClientEndpoint
public class ChessWebSocketClient {

    private Session session;
    private final Gson gson = new Gson();

    public void connect(String uri, String username, int gameId) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));

        // Send JOIN message after connection
        var joinMessage = gson.toJson(new ChessAction(ChessAction.ActionType.JOIN, username, gameId, null));
        session.getAsyncRemote().sendText(joinMessage);
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to WebSocket server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ChessNotification notification = gson.fromJson(message, ChessNotification.class);
        System.out.println("⚡ WebSocket message: " + notification.getMessage());
        // Later: trigger UI redraw, board update, etc.
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error:");
        throwable.printStackTrace();
    }

    public void sendMove(String username, int gameId, String move) {
        var moveMessage = gson.toJson(new ChessAction(ChessAction.ActionType.MOVE, username, gameId, move));
        session.getAsyncRemote().sendText(moveMessage);
    }

    public void close() throws Exception {
        if (session != null) session.close();
    }
}

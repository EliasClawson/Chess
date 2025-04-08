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

    public void connect(String uri, String username, int gameId) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));

        // Send JOIN message after connection
        String joinMessage = gson.toJson(new ChessAction(ChessAction.ActionType.JOIN, username, gameId, null));
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
        String moveMessage = gson.toJson(new ChessAction(ChessAction.ActionType.MOVE, username, gameId, move));
        session.getAsyncRemote().sendText(moveMessage);
    }

    public void sendLeave(String username, int gameId) {
        // Create a LEAVE message using ChessAction
        String leaveMessage = gson.toJson(
                new ChessAction(ChessAction.ActionType.LEAVE, username, gameId, null)
        );
        session.getAsyncRemote().sendText(leaveMessage);
    }

    public void sendResign(String username, int gameId) {
        String resignMessage = gson.toJson(
                new ChessAction(ChessAction.ActionType.RESIGN, username, gameId, null)
        );
        session.getAsyncRemote().sendText(resignMessage);
    }



    public void close() throws Exception {
        if (session != null) session.close();
    }
}

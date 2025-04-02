package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ChessNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // Map from gameId to a map of username to Connection.
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();

    // Add a connection for a given user and game.
    public void add(String username, int gameId, Session session) {
        gameConnections.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>());
        Connection connection = new Connection(username, session);
        gameConnections.get(gameId).put(username, connection);
    }

    // Remove a connection for a given user and game.
    public void remove(String username, int gameId) {
        if (gameConnections.containsKey(gameId)) {
            gameConnections.get(gameId).remove(username);
            if (gameConnections.get(gameId).isEmpty()) {
                gameConnections.remove(gameId);
            }
        }
    }

    // Broadcast a message to all connections in a game, optionally excluding one username.
    public void broadcast(int gameId, String excludeUsername, ChessNotification notification) throws IOException {
        if (!gameConnections.containsKey(gameId)) return;

        var connections = gameConnections.get(gameId);
        var removeList = new ArrayList<Connection>();

        for (var conn : connections.values()) {
            if (conn.session.isOpen()) {
                if (!conn.visitorName.equals(excludeUsername)) {
                    conn.send(notification.toString());
                }
            } else {
                removeList.add(conn);
            }
        }

        // Clean up any closed connections.
        for (var conn : removeList) {
            connections.remove(conn.visitorName);
        }
    }

    // Helper to broadcast to all without excluding any user.
    public void broadcast(int gameId, ChessNotification notification) throws IOException {
        broadcast(gameId, "", notification);
    }
}

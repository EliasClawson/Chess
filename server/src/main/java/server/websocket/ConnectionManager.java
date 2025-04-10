package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ChessNotification;
import websocket.messages.ServerMessage;

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
        System.out.println("Broadcasting to " + gameId + " excluding " + excludeUsername);
        if (!gameConnections.containsKey(gameId)) {return;}

        String json = new Gson().toJson(notification);
        var connections = gameConnections.get(gameId);
        var removeList = new ArrayList<Connection>();

        for (var conn : connections.values()) {
            if (conn.session.isOpen()) {
                System.out.println("Comparing " + conn.visitorName + " to " + excludeUsername);
                if (!conn.visitorName.equals(excludeUsername)) {
                    System.out.println("** Sending notification to " + conn.visitorName);
                    System.out.println("** JSON: " + json);
                    conn.send(json);
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

    public void broadcast(int gameId, ServerMessage message) throws IOException {
        if (!gameConnections.containsKey(gameId)) {return;}

        String json = new Gson().toJson(message);
        var connections = gameConnections.get(gameId);
        var removeList = new ArrayList<Connection>();

        for (var conn : connections.values()) {
            if (conn.session.isOpen()) {
                conn.send(json);
            } else {
                removeList.add(conn);
            }
        }

        for (var conn : removeList) {
            connections.remove(conn.visitorName);
        }
    }




    // Broadcast a raw JSON string (like a ServerMessage) to everyone in a game.
    public void broadcastRaw(int gameId, String message) throws IOException {
        if (!gameConnections.containsKey(gameId)) {return;}

        var connections = gameConnections.get(gameId);
        var removeList = new ArrayList<Connection>();

        for (var conn : connections.values()) {
            if (conn.session.isOpen()) {
                conn.send(message);
            } else {
                removeList.add(conn);
            }
        }

        for (var conn : removeList) {
            connections.remove(conn.visitorName);
        }
    }
    public void sendTo(int gameId, String username, ServerMessage message) throws IOException {
        if (!gameConnections.containsKey(gameId)) {return;}

        var connections = gameConnections.get(gameId);
        var conn = connections.get(username);

        if (conn != null && conn.session.isOpen()) {
            String jsonMessage = new Gson().toJson(message);
            conn.send(jsonMessage);
        }
    }



}

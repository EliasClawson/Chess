package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String visitorName;
    public Session session;

    public Connection(String visitorName, Session session) {
        this.visitorName = visitorName;
        this.session = session;
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
        System.out.println("📨 Sent to " + visitorName + ": " + message);
    }
}
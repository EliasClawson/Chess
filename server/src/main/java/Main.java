import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        // Start the server
        Server server = new Server(); // Creates a new server, defined by Server.java
        server.run(8080);  // Run on port 8080
    }
}

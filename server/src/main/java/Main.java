import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        // Start the server
        Server server = new Server();
        server.run(8080);  // Run on port 8080

        System.out.println("♕ 240 Chess Server Started on port 8080");
    }
}

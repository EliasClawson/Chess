package server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 8080; // Start the server on port 8080 as required
        System.out.println("Starting server on port " + port + "...");
        server.run(port);


        System.out.println("♕ 240 Chess Server started on port " + port);
    }
}

import chess.*;
import client.ChessClientUI;
import client.ServerFacade;

public class Main {
    public static void main(String[] args) {
        // Assuming your server is running on port 8080 (or configure as needed)
        int serverPort = 8080;
        ServerFacade facade = new ServerFacade(serverPort);
        ChessClientUI clientUI = new ChessClientUI(facade);
        clientUI.run(); // This will start your client command loop.
    }
}


// TODO: Joining as white or black didn't work
// TODO: Board appears wrong
// What's supposed to happen when you join a game?
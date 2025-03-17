import chess.*;
import client.ChessClientUI;
import client.ServerFacade;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // Assuming your server is running on port 8080 (or configure as needed)
        int serverPort = 8080;
        ServerFacade facade = new ServerFacade(serverPort);
        ChessClientUI clientUI = new ChessClientUI(facade);
        clientUI.run(); // This will start your client command loop.
    }
}
import chess.*;
import client.ChessClientUI;
import client.ServerFacade;

import java.io.IOException;

// This is the client's main
public class Main {
    public static void main(String[] args) throws IOException {
        int serverPort = 8080; // Hook up with my server
        ServerFacade facade = new ServerFacade(serverPort);
        ChessClientUI clientUI = new ChessClientUI(facade);
        clientUI.run(); // Begin command loop

    }
}
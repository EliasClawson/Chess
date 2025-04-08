package client;

import java.util.List;
import java.util.Scanner;

import chess.ChessBoard;
import com.google.gson.Gson;
import model.AuthData;
import model.GameInfo;
import ui.ChessBoardRenderer;
import client.ClientErrorResponse;
import java.io.IOException;
import chess.ChessGame;

public class ChessClientUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    // You might want to store the currently logged-in user here.
    private AuthData currentUser;
    // For drawing the board:
    private final ChessBoardRenderer boardRenderer;
    private ChessWebSocketClient webSocketClient;

    private List<GameInfo> lastGames;
    private boolean inGame;
    private boolean isPlayerWhite;
    private boolean isObserver;
    private int currentGameID;
    private int currentGameNumber;

    public ChessClientUI(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
        this.boardRenderer = new ChessBoardRenderer();
        this.inGame = false;
    }

    public void run() throws IOException {
        // Main loop: switch between prelogin and postlogin
        while (true) {
            if (currentUser == null) {
                preloginMenu();
            } else if (!inGame){
                postloginMenu();
            } else {
                inGameMenu();
            }
        }
    }

    private void preloginMenu() throws IOException {
        System.out.println("=== Prelogin Menu ===");
        System.out.println("1. Help");
        System.out.println("2. Register");
        System.out.println("3. Login");
        System.out.println("4. Quit");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                printPreloginHelp();
                break;
            case "2":
                doRegister();
                break;
            case "3":
                doLogin();
                break;
            case "4":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            case "314":
                System.out.println("Clearing database for testing...");
                facade.clearDatabase();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void postloginMenu() {
        System.out.println("=== Postlogin Menu ===");
        System.out.println("1. Help");
        System.out.println("2. Logout");
        System.out.println("3. Create Game");
        System.out.println("4. List Games");
        System.out.println("5. Play Game");
        System.out.println("6. Observe Game");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                printPostloginHelp();
                break;
            case "2":
                doLogout();
                break;
            case "3":
                doCreateGame();
                break;
            case "4":
                doListGames();
                break;
            case "5":
                doJoinGame();  // for play game
                break;
            case "6":
                doObserveGame(); // might simply draw the board
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void inGameMenu() {
        System.out.println("=== In Game Menu ===");
        System.out.println("1. Help");
        System.out.println("2. Redraw Chess Board");
        System.out.println("3. Leave");
        System.out.println("4. Make Move");
        System.out.println("5. Resign");
        System.out.println("6. Highlight Legal Moves");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                printInGameHelp();
                break;
            case "2":
                doRedrawBoard();
                break;
            case "3":
                doLeaveGame();
                break;
            case "4":
                doMakeMove();
                break;
            case "5":
                doResignGame();
                break;
            case "6":
                doHighlightMoves();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Implement each command's logic (calling facade methods, handling input, etc.)
    private void doRegister() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            AuthData auth = facade.register(username, password, email);
            System.out.println("Registered successfully as " + auth.getUsername());
            currentUser = auth;
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doLogin() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            AuthData auth = facade.login(username, password);
            System.out.println("Logged in successfully as " + auth.getUsername());
            currentUser = auth;
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doLogout() {
        try {
            facade.logout(currentUser.getAuthToken());
            System.out.println("Logged out successfully.");
            currentUser = null;
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doCreateGame() {
        try {
            System.out.print("Enter game name: ");
            String gameName = scanner.nextLine().trim();
            int gameID = facade.createGame(currentUser.getAuthToken(), gameName);
            System.out.println("Game created with name: " + gameName);
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doListGames() {
        try {
            lastGames = facade.listGames(currentUser.getAuthToken());
            if (lastGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }
            System.out.println("Available Games:");
            int i = 1;
            for (GameInfo game : lastGames) {
                // You can choose what information to show for each game.
                // For example, game name and the players (if any).
                System.out.println(i + ". " + game.gameName() + " (White: "
                        + (game.whiteUsername() == null ? "None" : game.whiteUsername())
                        + ", Black: "
                        + (game.blackUsername() == null ? "None" : game.blackUsername())
                        + ")");
                i++;
            }
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }


    private void doJoinGame() {
        try {
            lastGames = facade.listGames(currentUser.getAuthToken());
            if (lastGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }
            int gameNum;
            while (true) {
                try {
                    System.out.print("Enter the game number to join: ");
                    gameNum = Integer.parseInt(scanner.nextLine().trim());
                    if (gameNum < 1 || gameNum > lastGames.size()) {
                        System.out.println("Invalid game number. Please enter a number between 1 and " + lastGames.size());
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid integer.");
                }
            }

            // Look up the gameID from the list based on the display number.
            int gameID = lastGames.get(gameNum - 1).gameID();

            String color = "";
            while (true) {
                System.out.print("Join as (WHITE/BLACK): ");
                color = scanner.nextLine().trim();
                if (color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("W")) {
                    break;
                } else if (color.equalsIgnoreCase("BLACK") || color.equalsIgnoreCase("B")) {
                    break;
                } else {
                    System.out.println("Invalid input. Please enter either 'WHITE' or 'BLACK'.");
                }
            }

            boolean joinAsWhite = color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("W");
            facade.joinGame(currentUser.getAuthToken(), gameID, joinAsWhite);
            System.out.println("Joined game " + gameNum + " as " + (joinAsWhite ? "WHITE" : "BLACK"));
            //boardRenderer.renderBoard(!joinAsWhite);
            this.inGame = true;
            this.currentGameID = gameID;
            this.currentGameNumber = gameNum;
            this.isPlayerWhite = joinAsWhite;
            this.isObserver = false;

            //Start websocket connection
            // Start websocket connection for joining as a player:
            try {
                String username = currentUser.getUsername();
                String wsUrl = "ws://localhost:8081/"; // ensure this matches your server setup
                // Use gameNum (the display number) for displayGameNumber,
                // and set role based on joinAsWhite.
                String displayGameNumber = String.valueOf(gameNum);
                String role = joinAsWhite ? "WHITE" : "BLACK";

                webSocketClient = new ChessWebSocketClient();
                // Use the new overloaded connect() method:
                webSocketClient.connect(wsUrl, username, gameID, displayGameNumber, role);

                System.out.println("🎉 Connected to game " + displayGameNumber);
                doRedrawBoard();  // Refresh board display with real state

            } catch (Exception e) {
                System.err.println("WebSocket connection failed:");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doLeaveGame() {
        try {
            // Only call the REST update if the user is a player
            if (!isObserver) {
                facade.leaveGame(currentUser.getAuthToken(), currentGameID);
                System.out.println("You have left the game (REST update successful).");
            } else {
                System.out.println("As an observer, no REST update is needed.");
            }

            if (webSocketClient != null) {
                String displayGameNumber = String.valueOf(currentGameNumber);
                String username = currentUser.getUsername();
                String role = isObserver ? "Observer" : isPlayerWhite ? "WHITE" : "BLACK";
                webSocketClient.sendLeave(username, currentGameID, displayGameNumber, role);

                // Wait for leave acknowledgement
                for (int i = 0; i < 10; i++) {
                    if (webSocketClient.leaveAcknowledged) break;
                    Thread.sleep(50);
                }

                webSocketClient.close();
                webSocketClient = null;
            }
            inGame = false;
        } catch (Exception e) {
            System.out.println("Error leaving game: " + extractErrorMessage(e.getMessage()));
        }
    }



    private void doResignGame() {
        try {
            facade.resignGame(currentUser.getAuthToken(), currentGameID);
            System.out.println("You have resigned the game (REST update successful).");

            if (webSocketClient != null) {
                // Use the new sendResign method:
                String displayGameNumber = String.valueOf(currentGameID); // adjust if needed
                String role = isPlayerWhite ? "WHITE" : "BLACK";
                webSocketClient.sendResign(currentUser.getUsername(), currentGameID, displayGameNumber, role);
                webSocketClient.close();
                webSocketClient = null;
            }
            inGame = false;
        } catch (Exception e) {
            System.out.println("Error resigning game: " + extractErrorMessage(e.getMessage()));
        }
    }





    private void doObserveGame() {
        try {
            lastGames = facade.listGames(currentUser.getAuthToken());
            if (lastGames.isEmpty()) {
                System.out.println("No games available.");
                return;
            }
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
            return;
        }

        int gameNum;
        while (true) {
            try {
                System.out.print("Enter the game number to observe: ");
                gameNum = Integer.parseInt(scanner.nextLine().trim());
                if (gameNum < 1 || gameNum > lastGames.size()) {
                    System.out.println("Invalid game number. Please enter a number between 1 and " + lastGames.size());
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }

        int gameID = lastGames.get(gameNum - 1).gameID();
        this.currentGameID = gameID;
        this.currentGameNumber = gameNum;
        this.isObserver = true;
        this.isPlayerWhite = true; // Note really but just to display

        // Optionally retrieve and show game state
        try {
            doRedrawBoard();
        } catch (Exception e) {
            System.out.println("Error fetching game state: " + extractErrorMessage(e.getMessage()));
        }

        // Open a WebSocket connection using the standard JOIN action.
        try {
            String wsUrl = "ws://localhost:8081/"; // Use your WebSocket server port
            webSocketClient = new ChessWebSocketClient();
            String username = currentUser.getUsername();
            String role = "Observer";
            String displayGameNumber = String.valueOf(gameNum);

            // Notice: we use the normal join (not a separate OBSERVE action)
            webSocketClient.connect(wsUrl, username, gameID, displayGameNumber, role);
            System.out.println("Now observing game " + gameNum);
            inGame = true;
        } catch (Exception e) {
            System.err.println("WebSocket connection failed:");
            e.printStackTrace();
        }
    }


    private void doRedrawBoard() {
        try {
            //System.out.println("Redrawing board with messy ID: " + currentGameID);
            ChessBoard gameBoard  = facade.getGameState(currentUser.getAuthToken(), currentGameID);
            //System.out.println("Got this board from the server:\n" + gameBoard);
            boardRenderer.renderBoard(gameBoard, !isPlayerWhite);
        } catch (Exception e) {
            System.out.println("Failed to redraw board: " + extractErrorMessage(e.getMessage()));
        }
    }

    private void doHighlightMoves() {
        try {
            // Prompt for a position input (e.g., "e2")
            System.out.print("Enter board position of piece to highlight legal moves:");
            String posInput = scanner.nextLine().trim();
            chess.ChessPosition pos = parsePosition(posInput);

            // Retrieve the current full game state from the server.
            // You can call facade.getGameState() if it returns a ChessGame.
            // Otherwise, maintain your local game state.
            ChessGame gameState = facade.getFullGameState(currentUser.getAuthToken(), currentGameID);
            // Alternatively, if your facade.getGameState returns only a ChessBoard,
            // ensure you have a way to get the full ChessGame.

            // Compute legal moves using the game logic.
            // validMoves(...) returns a Collection<ChessMove>.
            // NOTE: Your ChessGame.validMoves(pos) method exists as per your ChessGame.java.
            // (Assuming that gameState is an instance of ChessGame)
            var legalMoves = gameState.validMoves(pos);
            if (legalMoves == null || legalMoves.isEmpty()) {
                System.out.println("No legal moves available for the piece at " + posInput);
            } else {
                System.out.println("Legal moves:");
                for (var move : legalMoves) {
                    System.out.println("From " + move.getStartPosition() + " to " + move.getEndPosition());
                }
            }

            // Now redraw the board, highlighting the squares of legal moves.
            // For this, modify your board renderer method to accept a collection of moves to highlight.
            // For example, add an overloaded renderBoard method:
            boardRenderer.renderBoardHighlights(gameState.getBoard(), !isPlayerWhite, legalMoves);

        } catch (Exception e) {
            System.out.println("Error highlighting moves: " + e.getMessage());
        }
    }

    // This method should run from within your ChessClientUI
    private void doMakeMove() {
        try {
            // Ensure player (not observer) and that it's their turn.
            if (isObserver) {
                System.out.println("Observers cannot make moves.");
                return;
            }

            // Retrieve the full game state.
            chess.ChessGame gameState = facade.getFullGameState(currentUser.getAuthToken(), currentGameID);
            if ( (isPlayerWhite && gameState.getTeamTurn() != chess.ChessGame.TeamColor.WHITE) ||
                    (!isPlayerWhite && gameState.getTeamTurn() != chess.ChessGame.TeamColor.BLACK) ) {
                System.out.println("It's not your turn.");
                return;
            }

            // Prompt for move string.
            System.out.print("Enter your move (e.g., e2e4): ");
            String moveInput = scanner.nextLine().trim();

            // (Optional) Parse move string to validate format.
            chess.ChessMove move = parseMove(moveInput);

            // Send move over WebSocket.
            webSocketClient.sendMove(currentUser.getUsername(), currentGameID, moveInput);
            System.out.println("Move sent: " + moveInput);

            doRedrawBoard();
        } catch (Exception e) {
            System.out.println("Error making move: " + e.getMessage());
        }
    }



    private chess.ChessMove parseMove(String input) throws IllegalArgumentException {
        // Expect input of exactly 4 characters (e.g., "e2e4")
        if (input == null || input.length() != 4) {
            throw new IllegalArgumentException("Please enter a move in the format 'e2e4'.");
        }
        String startPart = input.substring(0, 2);
        String endPart = input.substring(2, 4);

        chess.ChessPosition start = parsePosition(startPart);
        chess.ChessPosition end = parsePosition(endPart);

        // Create a ChessMove; if your constructor requires a promotion piece, you can pass null.
        return new chess.ChessMove(start, end, null);
    }



    // Example helper inside ChessClientUI or a separate utility class.
    private chess.ChessPosition parsePosition(String pos) throws IllegalArgumentException {
        // Expect pos in the format "e2" (column letter, row number)
        if (pos == null || pos.length() != 2) {
            throw new IllegalArgumentException("Invalid format. Use a letter (a-h) followed by a number (1-8).");
        }
        char colChar = Character.toLowerCase(pos.charAt(0));
        int row = Character.getNumericValue(pos.charAt(1));
        if (colChar < 'a' || colChar > 'h' || row < 1 || row > 8) {
            throw new IllegalArgumentException("Position out of bounds. Columns a-h and rows 1-8.");
        }
        // Convert letter to a column number (a = 1, b = 2, ...)
        int col = colChar - 'a' + 1;
        return new chess.ChessPosition(row, col);
    }


    private void printPreloginHelp() {
        System.out.println("Prelogin Help:");
        System.out.println(" - Register: Create a new user account.");
        System.out.println(" - Login: Log in to your account.");
        System.out.println(" - Quit: Exit the application.");
    }

    private void printPostloginHelp() {
        System.out.println("Postlogin Help:");
        System.out.println(" - Logout: Log out of your account.");
        System.out.println(" - Create Game: Create a new game on the server.");
        System.out.println(" - List Games: Display a list of current games.");
        System.out.println(" - Play Game: Join a game as a player.");
        System.out.println(" - Observe Game: Watch a game (for now, just draw the board).");
    }

    private void printInGameHelp() {
        System.out.println("In Game Help:");
        System.out.println(" - Redraw chess board: Reload the current chessboard.");
        System.out.println(" - Leave: Exit the current game to the previous menu");
        System.out.println(" - Make Move: Make a move in the current game.");
        System.out.println(" - Resign: Resign the current game. Ends Game.");
        System.out.println(" - Highlight Legal Moves: Enter a piece and highlight legal moves on the board.");
    }

    private String extractErrorMessage(String jsonMessage) {
        try {
            Gson gson = new Gson();
            ClientErrorResponse errorResponse = gson.fromJson(jsonMessage, ClientErrorResponse.class);
            return errorResponse.message;
        } catch (Exception e) {
            // If parsing fails, just return the original message.
            return jsonMessage;
        }
    }

}

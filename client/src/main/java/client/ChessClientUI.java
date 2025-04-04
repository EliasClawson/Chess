package client;

import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import model.AuthData;
import model.GameInfo;
import ui.ChessBoardRenderer;
import client.ClientErrorResponse;
import java.io.IOException;

public class ChessClientUI {

    private final ServerFacade facade;
    private final Scanner scanner;
    // You might want to store the currently logged-in user here.
    private AuthData currentUser;
    // For drawing the board:
    private final ChessBoardRenderer boardRenderer;

    private List<GameInfo> lastGames;
    private boolean inGame;

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
            boardRenderer.renderBoard(!joinAsWhite);
            this.inGame = true;

            //Start websocket connection
            ChessWebSocketClient webSocketClient = new ChessWebSocketClient();
            String wsUrl = "ws://localhost:8081/";  // Match your server WebSocket port
            webSocketClient.connect(wsUrl, currentUser.getUsername(), gameID);

        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
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
                break; // parsing succeeded
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }

        // For now, simply draw the chessboard (or print a message)
        System.out.println("Observing game - drawing initial chessboard:");
        boardRenderer.renderBoard(false);
    }

    private void doRedrawBoard() {
        boardRenderer.renderBoard(false);
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

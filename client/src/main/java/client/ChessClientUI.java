package client;

import java.util.Scanner;

import com.google.gson.Gson;
import model.AuthData;
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

    public ChessClientUI(ServerFacade facade) {
        this.facade = facade;
        this.scanner = new Scanner(System.in);
        this.boardRenderer = new ChessBoardRenderer();
    }

    public void run() throws IOException {
        // Main loop: switch between prelogin and postlogin
        while (true) {
            if (currentUser == null) {
                preloginMenu();
            } else {
                postloginMenu();
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
            System.out.println("Game created with ID: " + gameID);
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doListGames() {
        try {
            var games = facade.listGames(currentUser.getAuthToken());
            System.out.println("Available Games:");
            // For now, just print them (you might want to number them)
            int i = 1;
            for (Object game : games) {
                System.out.println(i + ". " + game.toString());
                i++;
            }
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }

    private void doJoinGame() {
        try {
            System.out.print("Enter the game number to join: ");
            int gameNum = Integer.parseInt(scanner.nextLine().trim());

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
            facade.joinGame(currentUser.getAuthToken(), gameNum, joinAsWhite);
            System.out.println("Joined game " + gameNum + " as " + (joinAsWhite ? "WHITE" : "BLACK"));
            boardRenderer.renderBoard(!joinAsWhite);
        } catch (Exception e) {
            System.out.println(extractErrorMessage(e.getMessage()));
        }
    }


    private void doObserveGame() {
        System.out.print("Enter the game number to observe: ");
        int gameNum = Integer.parseInt(scanner.nextLine().trim());

        // For now, simply draw the chessboard (or print a message)
        System.out.println("Observing game - drawing initial chessboard:");
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

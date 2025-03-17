package ui;

import ui.EscapeSequences;

public class ChessBoardRenderer {

    public void renderBoard(boolean isBlack) {
        // Clear the screen
        System.out.print(EscapeSequences.ERASE_SCREEN);

        if(isBlack) {
            // Print top column labels with fixed spacing
            System.out.println("   H  G  F  E  D  C  B  A");
            for (int row = 1; row <= 8; row++) {
                // Print left row number with a space
                System.out.printf("%d ", row);
                for (int col = 8; col >= 1; col--) {
                    boolean isLight = ((row + col) % 2 == 1);
                    String bgColor = isLight ? EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
                    // Use formatted output to ensure each square takes 3 characters.
                    // Here we print a space (or piece) padded to 3 characters.
                    System.out.printf("%s%-3s%s", bgColor, " ", EscapeSequences.RESET_BG_COLOR);
                }
                // Print right row number
                System.out.printf(" %d%n", row);
            }
            // Print bottom column labels with fixed spacing
            System.out.println("   H  G  F  E  D  C  B  A");
        } else {
            // Print top column labels with fixed spacing
            System.out.println("   A  B  C  D  E  F  G  H");
            for (int row = 8; row >= 1; row--) {
                // Print left row number with a space
                System.out.printf("%d ", row);
                for (int col = 1; col <= 8; col++) {
                    boolean isLight = ((row + col) % 2 == 1);
                    String bgColor = isLight ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                    // Use formatted output to ensure each square takes 3 characters.
                    // Here we print a space (or piece) padded to 3 characters.
                    System.out.printf("%s%-3s%s", bgColor, " ", EscapeSequences.RESET_BG_COLOR);
                }
                // Print right row number
                System.out.printf(" %d%n", row);
            }
            // Print bottom column labels with fixed spacing
            System.out.println("   A  B  C  D  E  F  G  H");
        }
    }
}

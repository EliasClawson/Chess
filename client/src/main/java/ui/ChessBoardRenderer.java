package ui;

import ui.EscapeSequences;

public class ChessBoardRenderer {

    // For simplicity, assume the board is an 8x8 array of strings.
    // In a real game, you might have a more complex data structure.
    public void renderBoard() {
        // Clear the screen
        System.out.print(EscapeSequences.ERASE_SCREEN);

        // Draw column letters
        System.out.println("  A B C D E F G H");
        // Loop over 8 rows
        for (int row = 8; row >= 1; row--) {
            // Print row number
            System.out.print(row + " ");
            for (int col = 1; col <= 8; col++) {
                // For now, print empty squares
                // Alternate square colors using ANSI sequences
                boolean isLight = ((row + col) % 2 == 0);
                String bgColor = isLight ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                System.out.print(bgColor + EscapeSequences.EMPTY + EscapeSequences.RESET_BG_COLOR);
            }
            System.out.println(" " + row);
        }
        // Draw column letters at the bottom
        System.out.println("  A B C D E F G H");
    }
}

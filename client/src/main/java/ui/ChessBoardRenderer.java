package ui;

import ui.EscapeSequences;

import static ui.EscapeSequences.*;

public class ChessBoardRenderer {

    public void renderBoard(boolean isBlack) {
        // Clear the screen
        System.out.print(EscapeSequences.ERASE_SCREEN);

        if(isBlack) {
            // Print top column labels with fixed spacing
            System.out.println("   H 　G 　F 　E 　D 　C 　B 　A");
            for (int row = 1; row <= 8; row++) {
                // Print left row number with a space
                System.out.printf("%d ", row);
                for (int col = 8; col >= 1; col--) {
                    renderPieces(row, col);
                }
                // Print right row number
                System.out.printf(" %d%n", row);
            }
            // Print bottom column labels with fixed spacing
            System.out.println("   H 　G 　F 　E 　D 　C 　B 　A");
        } else {
            // Print top column labels with fixed spacing
            System.out.println("   A 　B 　C 　D 　E 　F 　G 　H");
            for (int row = 8; row >= 1; row--) {
                // Print left row number with a space
                System.out.printf("%d ", row);
                for (int col = 1; col <= 8; col++) {
                    renderPieces(row, col);
                }
                // Print right row number
                System.out.printf(" %d%n", row);
            }
            // Print bottom column labels with fixed spacing
            System.out.println("   A 　B 　C 　D 　E 　F 　G 　H");
        }
    }

    public void renderPieces(int row, int col) {
        boolean isLight = ((row + col) % 2 == 1);
        String bgColor = isLight ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        if(row == 2) {
            System.out.printf("%s%3s%s", bgColor, BLACK_PAWN, EscapeSequences.RESET_BG_COLOR);
        } else if(row == 7) {
            System.out.printf("%s%3s%s", bgColor, WHITE_PAWN, EscapeSequences.RESET_BG_COLOR);
        } else if(row == 8) {
            if(col == 1 || col == 8) {
                System.out.printf("%s%3s%s", bgColor, WHITE_ROOK, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 2 || col == 7) {
                System.out.printf("%s%3s%s", bgColor, WHITE_KNIGHT, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 3 || col == 6) {
                System.out.printf("%s%3s%s", bgColor, WHITE_BISHOP, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 4) {
                System.out.printf("%s%3s%s", bgColor, WHITE_QUEEN, EscapeSequences.RESET_BG_COLOR);
            } else {
                System.out.printf("%s%3s%s", bgColor, WHITE_KING, EscapeSequences.RESET_BG_COLOR);
            }
        } else if(row == 1) {
            if(col == 1 || col == 8) {
                System.out.printf("%s%3s%s", bgColor, BLACK_ROOK, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 2 || col == 7) {
                System.out.printf("%s%3s%s", bgColor, BLACK_KNIGHT, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 3 || col == 6) {
                System.out.printf("%s%3s%s", bgColor, BLACK_BISHOP, EscapeSequences.RESET_BG_COLOR);
            } else if(col == 4) {
                System.out.printf("%s%3s%s", bgColor, BLACK_QUEEN, EscapeSequences.RESET_BG_COLOR);
            } else {
                System.out.printf("%s%3s%s", bgColor, BLACK_KING, EscapeSequences.RESET_BG_COLOR);
            }
        } else {
            System.out.printf("%s%3s%s", bgColor, EMPTY, EscapeSequences.RESET_BG_COLOR);
        }
    }
}

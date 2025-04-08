package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import ui.EscapeSequences;

import java.util.Collection;

import static ui.EscapeSequences.*;

public class ChessBoardRenderer {

    public void renderBoard(ChessBoard board, boolean isBlack, Collection<ChessMove> highlights) {
        // Clear the screen
        System.out.print(EscapeSequences.ERASE_SCREEN);

        // Determine board drawing order based on isBlack
        if (isBlack) {
            System.out.println("   H  G  F  E  D  C  B  A");
            for (int row = 1; row <= 8; row++) {
                System.out.printf("%d ", row);
                for (int col = 8; col >= 1; col--) {
                    renderPiece(board, row, col, highlights);
                }
                System.out.printf(" %d%n", row);
            }
            System.out.println("   H  G  F  E  D  C  B  A");
        } else {
            System.out.println("   A  B  C  D  E  F  G  H");
            for (int row = 8; row >= 1; row--) {
                System.out.printf("%d ", row);
                for (int col = 1; col <= 8; col++) {
                    renderPiece(board, row, col, highlights);
                }
                System.out.printf(" %d%n", row);
            }
            System.out.println("   A  B  C  D  E  F  G  H");
        }
    }

    // New helper method that uses the board and checks if a square should be highlighted
    private void renderPiece(ChessBoard board, int row, int col, Collection<ChessMove> highlights) {
        // Check if the square (row, col) is among the destination positions in any legal move.
        boolean highlight = false;
        for (var move : highlights) {
            if (move.getEndPosition().getRow() == row && move.getEndPosition().getColumn() == col) {
                highlight = true;
                break;
            }
        }

        // Determine background color: if highlight, use a bright highlight color; else normal light/dark.
        String bgColor;
        if (highlight) {
            bgColor = EscapeSequences.SET_TEXT_COLOR_YELLOW; // For example; define this in your EscapeSequences.
        } else {
            boolean isLight = ((row + col) % 2 == 1);
            bgColor = isLight ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        }

        // Get the piece at the given position.
        var piece = board.getPiece(new chess.ChessPosition(row, col));
        String pieceStr;
        if (piece == null) {
            pieceStr = EscapeSequences.EMPTY;
        } else {
            pieceStr = getSymbol(piece);  // your getSymbol method that returns piece icons.
        }
        System.out.printf("%s%3s%s", bgColor, pieceStr, EscapeSequences.RESET_BG_COLOR);
    }


    // New helper method that uses the board model
    private void renderPiece(ChessBoard board, int row, int col) {
        // Retrieve the piece at the given position using your ChessBoard API.
        ChessPiece piece = board.getPiece(new chess.ChessPosition(row, col));
        String bgColor = (((row + col) % 2) == 1) ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        String pieceString;
        if (piece == null) {
            pieceString = EscapeSequences.EMPTY;
        } else {
            // You might use piece.getSymbol() if available, or build a string based on piece type and color.
            pieceString = getSymbol(piece);
        }
        System.out.printf("%s%3s%s", bgColor, pieceString, EscapeSequences.RESET_BG_COLOR);
    }


    public String getSymbol(ChessPiece piece) {
        switch (piece.getPieceType()) {
            case KING:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KING : BLACK_KING;
            case QUEEN:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_ROOK : BLACK_ROOK;
            case PAWN:
                return (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PAWN : BLACK_PAWN;
            default:
                return "";
        }
    }




    // OLD HELPER METHOD
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

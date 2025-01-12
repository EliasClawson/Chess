package chess;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor teamTurn;
    ChessBoard gameBoard;

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard(); // Setup for new game

        // Set the starting turn, always white
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece selectedPiece = gameBoard.getPiece(startPosition);
        if (selectedPiece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = selectedPiece.pieceMoves(gameBoard, startPosition);
        System.out.println("Checking if " + selectedPiece.getPieceType() + " at " + startPosition.getRow() + "," + startPosition.getColumn() + " can move");
        System.out.println("King is at " + getKing(selectedPiece.getTeamColor()).getRow() + ", " + getKing(selectedPiece.getTeamColor()).getColumn());
        possibleMoves = possibleMoves.stream() // Run through each possible move and check for check
                .filter(this::isMoveSafe)
                .collect(Collectors.toList());
        // System.out.println("Valid moves for " + selectedPiece.getPieceType() + " at " + startPosition + ": " + possibleMoves);
        System.out.println("Valid moves for " + selectedPiece.getPieceType() + " at " + startPosition + ": " + possibleMoves.size());
        return possibleMoves;
    }

    // Helper function to check if moving would cause check on your king
    private boolean isMoveSafe(ChessMove move) {
        // Original pieces
        ChessPiece movingPiece = gameBoard.getPiece(move.getStartPosition());
        ChessPiece capturedPiece = gameBoard.getPiece(move.getEndPosition());

        // Simulate the move (Undo this in a few lines)
        gameBoard.removePiece(move.getStartPosition());
        gameBoard.addPiece(move.getEndPosition(), movingPiece);

        // Check if the move puts the king in check
        boolean isSafe = !isInCheck(movingPiece.getTeamColor());

        // Revert the move
        gameBoard.removePiece(move.getEndPosition());
        gameBoard.addPiece(move.getStartPosition(), movingPiece);
        if (capturedPiece != null) {
            gameBoard.addPiece(move.getEndPosition(), capturedPiece);
        }

        System.out.println("Checking move: " + move + " with piece: " + movingPiece.getPieceType() + " - Is safe: " + isSafe);

        return isSafe;
    }



    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKing(teamColor);
        if(kingPosition == null) { return false;} // Skip check if there's no king

        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece == null || piece.getTeamColor() == teamColor) { continue; } // Skip if space empty or friendly

            Collection<ChessMove> pieceMoves = piece.pieceMoves(gameBoard, position);
            for (ChessMove move : pieceMoves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    System.out.println(teamColor + " in check at " + position.getRow() + "," + position.getColumn() + " with " + piece.getTeamColor() + " " + piece.getPieceType());
                    return true;
                }
            }
        }
        return false;
    }


    // Helper method to find the king for the team
    private ChessPosition getKing(TeamColor teamColor) {
        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                return position;
            }
        }
        System.out.println(teamColor + " King not currently on board");
        return null;
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}

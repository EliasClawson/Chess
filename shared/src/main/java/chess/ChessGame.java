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

        // Adding en passant here, so we can check the gameboard and get the surrounding piece information
        if (selectedPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            // IF STATMENT HERE TO CHECK FOR RIGHT EDGE (also down for left edge)
            if (startPosition.getColumn() < 8) {
                ChessPiece lookRight = gameBoard.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1));
                System.out.println("Made it here, don't know more yet");
                if (lookRight != null && lookRight.getPassantable()
                        && lookRight.getTeamColor() != selectedPiece.getTeamColor()
                        && startPosition.getColumn() < 8) {
                    System.out.println("enpassant possible - Right: " + lookRight.getPieceType());
                    possibleMoves.add(new ChessMove(startPosition, new ChessPosition(
                            startPosition.getRow() + (selectedPiece.getTeamColor() == TeamColor.WHITE ? 1 : -1), // Move diagonally forward
                            startPosition.getColumn() + 1),
                            null));
                }
            }
            if (startPosition.getColumn() > 1) {
                ChessPiece lookLeft = gameBoard.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1));
                if (lookLeft != null
                        && lookLeft.getPassantable() && lookLeft.getTeamColor() != selectedPiece.getTeamColor()
                        && startPosition.getColumn() > 1) {
                    System.out.println("enpassant possible - Left: " + lookLeft.getPieceType());
                    possibleMoves.add(new ChessMove(startPosition, new ChessPosition(
                            startPosition.getRow() + (selectedPiece.getTeamColor() == TeamColor.WHITE ? 1 : -1), // Move diagonally forward
                            startPosition.getColumn() - 1),
                            null));
                }
            }
        }

        // Adding castling moves here
        if (selectedPiece.getPieceType() == ChessPiece.PieceType.KING && selectedPiece.getNoMoves()) {
            // Check for kingside castling
            if (canCastleKingside(startPosition)) {
                possibleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2), null));
            }
            // Check for queenside castling
            if (canCastleQueenside(startPosition)) {
                possibleMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2), null));
            }
        }




        System.out.println("Checking if " + selectedPiece.getPieceType()
                + " at " + startPosition.getRow() + "," + startPosition.getColumn() + " can move");
        System.out.println(selectedPiece.getTeamColor()
                + " King is at " + getKing(selectedPiece.getTeamColor()).getRow()
                + ", " + getKing(selectedPiece.getTeamColor()).getColumn());
        possibleMoves = possibleMoves.stream() // Run through each possible move and check for check
                .filter(this::isMoveSafe)
                .collect(Collectors.toList());
        // System.out.println("Valid moves for " + selectedPiece.getPieceType() + " at " + startPosition + ": " + possibleMoves);
        System.out.println("Valid moves for " + selectedPiece.getPieceType() + " at "
                + startPosition + ": " + possibleMoves.size());
        for (ChessMove move : possibleMoves) {
            System.out.println(gameBoard.getPiece(move.getStartPosition()).getPieceType()
                    + " at " + move.getStartPosition().getRow() + "," + move.getStartPosition().getColumn()
                    + " can move to " + move.getEndPosition().getRow() + "," + move.getEndPosition().getColumn());
        }
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

        //System.out.println("Checking move: " + move + " with piece: " + movingPiece.getPieceType() + " - Is safe: " + isSafe);

        return isSafe;
    }


    private boolean canCastleKingside(ChessPosition kingSpot) {
        ChessPosition rookPosition = new ChessPosition(kingSpot.getRow(), 8);
        ChessPiece rook = gameBoard.getPiece(rookPosition);

        // Check if the rook is eligible for castling
        if (rook == null || rook.getPieceType() != ChessPiece.PieceType.ROOK || !rook.getNoMoves()) {
            return false;
        }

        // Check if the spaces between the king and rook are empty
        for (int col = kingSpot.getColumn() + 1; col < 8; col++) {
            if (gameBoard.getPiece(new ChessPosition(kingSpot.getRow(), col)) != null) {
                return false;
            }
        }

        // Ensure the king doesn't pass through or land on attacked squares
        return !isInCheck(gameBoard.getPiece(kingSpot).getTeamColor()) &&
                !isSquareUnderAttack(kingSpot.getRow(), kingSpot.getColumn() + 1,
                        gameBoard.getPiece(kingSpot).getTeamColor()) &&
                !isSquareUnderAttack(kingSpot.getRow(), kingSpot.getColumn() + 2,
                        gameBoard.getPiece(kingSpot).getTeamColor());
    }

    // Helper functions to determine if castling is possible
    private boolean canCastleQueenside(ChessPosition kingSpot) {
        ChessPosition rookPosition = new ChessPosition(kingSpot.getRow(), 1);
        ChessPiece rook = gameBoard.getPiece(rookPosition);

        // Check if the rook is ineligible
        if (rook == null || rook.getPieceType() != ChessPiece.PieceType.ROOK || !rook.getNoMoves()) {
            return false;
        }

        // Check if the spaces between the king and rook are empty
        for (int col = kingSpot.getColumn() - 1; col > 1; col--) {
            if (gameBoard.getPiece(new ChessPosition(kingSpot.getRow(), col)) != null) {
                return false;
            }
        }

        // Ensure the king doesn't pass through or land on attacked squares
        return !isInCheck(gameBoard.getPiece(kingSpot).getTeamColor()) &&
                !isSquareUnderAttack(kingSpot.getRow(), kingSpot.getColumn() - 1,
                        gameBoard.getPiece(kingSpot).getTeamColor()) &&
                !isSquareUnderAttack(kingSpot.getRow(), kingSpot.getColumn() - 2,
                        gameBoard.getPiece(kingSpot).getTeamColor());
    }

    private boolean isSquareUnderAttack(int row, int col, TeamColor teamColor) {
        ChessPosition targetPosition = new ChessPosition(row, col);
        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece == null || piece.getTeamColor() == teamColor) {
                continue;
            }
            // Check if the piece can move to the target position
            Collection<ChessMove> potentialMoves = piece.pieceMoves(gameBoard, position);
            for (ChessMove move : potentialMoves) {
                if (move.getEndPosition().equals(targetPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = gameBoard.getPiece(move.getStartPosition());

        if (movingPiece == null) {
            throw new InvalidMoveException("No piece at the starting position.");
        } else if(movingPiece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Not your turn.");
        }

        // Get the valid moves for the piece
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        // Check if the move is in the valid moves collection
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        // Handle en passant kill
        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 1 &&
                gameBoard.getPiece(move.getEndPosition()) == null) { // Pawn moved diagonally but there's no target (en passant)
            int capturedRow = move.getStartPosition().getRow(); // Same row as the moving pawn
            int capturedCol = move.getEndPosition().getColumn(); // Column of the target
            gameBoard.removePiece(new ChessPosition(capturedRow, capturedCol));
        }

        // Handle Castling
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2) { // King moved 2, so must be castle
            // Right castling
            if (move.getEndPosition().getColumn() > move.getStartPosition().getColumn()) {
                ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), 8);
                ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(),
                        move.getEndPosition().getColumn() - 1);
                ChessPiece rook = gameBoard.getPiece(rookStart);
                gameBoard.removePiece(rookStart);
                gameBoard.addPiece(rookEnd, rook);
            }
            // Left castling
            else {
                ChessPosition rookStart = new ChessPosition(move.getStartPosition().getRow(), 1);
                ChessPosition rookEnd = new ChessPosition(move.getStartPosition().getRow(),
                        move.getEndPosition().getColumn() + 1);
                ChessPiece rook = gameBoard.getPiece(rookStart);
                gameBoard.removePiece(rookStart);
                gameBoard.addPiece(rookEnd, rook);
            }
        }

        movingPiece = move.getPromotionPiece() == null ? movingPiece : new ChessPiece(movingPiece.getTeamColor(),
                move.getPromotionPiece());

        // Do the move
        gameBoard.removePiece(move.getStartPosition());
        gameBoard.addPiece(move.getEndPosition(), movingPiece);

        // Setup for en passant
        // Reset all pawns passantable status (Cause you can only do it the turn after)
        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                piece.setPassantable(false);
            }
        }

        // Current pawn is passantable if it moved two squares forward
        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) == 2) {
            movingPiece.setPassantable(true);
        }

        // Log moves to make impossible to castle
        movingPiece.setNoMoves(false);

        // Change turn
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
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
                    // System.out.println(teamColor + " in check at " + position.getRow() + "," + position.getColumn() + " with " + piece.getTeamColor() + " " + piece.getPieceType());
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
        System.out.println("Checking Checkmate for " + teamColor);
        if(!isInCheck(teamColor)){ return false; }

        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece == null || piece.getTeamColor() != teamColor) { continue; } // Skip if space empty or friendly

            Collection<ChessMove> validMoves = validMoves(position); // Get all moves of this piece

            // Check if any move removes the check
            for (ChessMove move : validMoves) {
                if (isMoveSafe(move)) {
                    System.out.println(teamColor + " can still move: " +
                            gameBoard.getPiece(move.getStartPosition()).getPieceType());
                    return false; // Found a move that removes the check
                }
            }
        }
        System.out.println(teamColor + " is in Checkmate...");
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        System.out.println("Checking Stalemate for " + teamColor);
        if(isInCheck(teamColor)) {return false;}
        for (ChessPosition position : gameBoard.getAllPositions()) {
            ChessPiece piece = gameBoard.getPiece(position);
            if (piece == null || piece.getTeamColor() != teamColor) { continue; } // Skip if space empty or friendly

            if(!validMoves(position).isEmpty()) {
                System.out.println(teamColor + " has moves:");
                for (ChessMove move : validMoves(position)) {
                    System.out.println(gameBoard.getPiece(position).getPieceType()
                            + " at " + position.getRow() + "," + position.getColumn() + " can move to "
                            + move.getEndPosition().getRow() + "," + move.getEndPosition().getColumn());
                }
                return false;
            }
        }
        return true;
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

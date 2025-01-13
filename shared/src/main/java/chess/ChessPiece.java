package chess;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    private final PieceType type;
    private final ChessGame.TeamColor color;
    private Boolean passantable = false;
    private Boolean noMoves = true;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.color = pieceColor;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public Boolean getPassantable() { return passantable; }

    public void setPassantable(Boolean passantable) { this.passantable = passantable; }

    public Boolean getNoMoves() { return noMoves; }

    public void setNoMoves(Boolean noMoves) { this.noMoves = noMoves; }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<ChessMove>();

        switch (type) {
            case PAWN:
                // Split out to comply with quality code standards
                validMoves.addAll(addPawnMoves(board, myPosition));
                break;
            case ROOK:
                // Positive row movement
                addLinearMoves(validMoves, board, myPosition, 1, 0);
                // Negative row movement
                addLinearMoves(validMoves, board, myPosition, -1, 0);
                // Positive col movement
                addLinearMoves(validMoves, board, myPosition, 0, 1);
                // Negative col movement
                addLinearMoves(validMoves, board, myPosition, 0, -1);
                break;
            case KNIGHT:
                // Split out to comply with code quality standards
                validMoves.addAll(addKnightMoves(board, myPosition));
                break;
            case BISHOP:
                // Calculate bishop moves
                // Positive right movement
                addLinearMoves(validMoves, board, myPosition, 1, 1);
                // Negative right movement
                addLinearMoves(validMoves, board, myPosition, -1, 1);
                // Positive left movement
                addLinearMoves(validMoves, board, myPosition, 1, -1);
                // Negative right movement
                addLinearMoves(validMoves, board, myPosition, -1, -1);
                break;
            case QUEEN:
                // Positive row movement
                addLinearMoves(validMoves, board, myPosition, 1, 0);
                // Negative row movement
                addLinearMoves(validMoves, board, myPosition, -1, 0);
                // Positive col movement
                addLinearMoves(validMoves, board, myPosition, 0, 1);
                // Negative col movement
                addLinearMoves(validMoves, board, myPosition, 0, -1);
                // Positive right movement
                addLinearMoves(validMoves, board, myPosition, 1, 1);
                // Negative right movement
                addLinearMoves(validMoves, board, myPosition, -1, 1);
                // Positive left movement
                addLinearMoves(validMoves, board, myPosition, 1, -1);
                // Negative right movement
                addLinearMoves(validMoves, board, myPosition, -1, -1);
                break;
            case KING:
                // Straight moves
                ChessPosition forward = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                if (inBounds(forward) && (board.getPiece(forward) == null || board.getPiece(forward).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, forward, null));
                }

                ChessPosition back = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                if (inBounds(back) && (board.getPiece(back) == null || board.getPiece(back).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, back, null));
                }

                ChessPosition right = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1);
                if (inBounds(right) && (board.getPiece(right) == null || board.getPiece(right).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, right, null));
                }

                ChessPosition left = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1);
                if (inBounds(left) && (board.getPiece(left) == null || board.getPiece(left).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, left, null));
                }

                // Diagonal moves
                ChessPosition diagUpRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                if (inBounds(diagUpRight) && (board.getPiece(diagUpRight) == null || board.getPiece(diagUpRight).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, diagUpRight, null));
                }

                ChessPosition diagUpLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
                if (inBounds(diagUpLeft) && (board.getPiece(diagUpLeft) == null || board.getPiece(diagUpLeft).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, diagUpLeft, null));
                }

                ChessPosition diagDownRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                if (inBounds(diagDownRight) && (board.getPiece(diagDownRight) == null || board.getPiece(diagDownRight).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, diagDownRight, null));
                }

                ChessPosition diagDownLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
                if (inBounds(diagDownLeft) && (board.getPiece(diagDownLeft) == null || board.getPiece(diagDownLeft).getTeamColor() != color)) {
                    validMoves.add(new ChessMove(myPosition, diagDownLeft, null));
                }
                break;
            default:
                throw new RuntimeException("Invalid piece type");
        }
        return validMoves;
    }

    private Collection<ChessMove> addKnightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<>();

        // Calculate knight moves using hardcoded offsets
        ChessPosition upRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);
        if (inBounds(upRight)) {
            ChessPiece piece = board.getPiece(upRight);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, upRight, null));
            }
        }

        ChessPosition rightUp = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);
        if (inBounds(rightUp)) {
            ChessPiece piece = board.getPiece(rightUp);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, rightUp, null));
            }
        }

        // Repeat for other knight moves...
        ChessPosition upLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);
        if (inBounds(upLeft)) {
            ChessPiece piece = board.getPiece(upLeft);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, upLeft, null));
            }
        }

        ChessPosition leftUp = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);
        if (inBounds(leftUp)) {
            ChessPiece piece = board.getPiece(leftUp);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, leftUp, null));
            }
        }

        ChessPosition downRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);
        if (inBounds(downRight)) {
            ChessPiece piece = board.getPiece(downRight);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, downRight, null));
            }
        }

        ChessPosition rightDown = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);
        if (inBounds(rightDown)) {
            ChessPiece piece = board.getPiece(rightDown);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, rightDown, null));
            }
        }

        ChessPosition downLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);
        if (inBounds(downLeft)) {
            ChessPiece piece = board.getPiece(downLeft);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, downLeft, null));
            }
        }

        ChessPosition leftDown = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);
        if (inBounds(leftDown)) {
            ChessPiece piece = board.getPiece(leftDown);
            if (piece == null || piece.getTeamColor() != color) {
                validMoves.add(new ChessMove(myPosition, leftDown, null));
            }
        }
        return validMoves;
    }

    private Collection<ChessMove> addPawnMoves(ChessBoard board, ChessPosition myPosition) {
        // Calculate pawn moves
        List<ChessMove> validMoves = new ArrayList<ChessMove>();
        int moveDirection = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;

        ChessPosition oneStep = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn());
        if (inBounds(oneStep) && board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == 8 && color == ChessGame.TeamColor.WHITE || oneStep.getRow() == 1 && color == ChessGame.TeamColor.BLACK) {
                // Add all promotion moves
                addPromotions(validMoves, myPosition, oneStep);
            } else {
                validMoves.add(new ChessMove(myPosition, oneStep, null));

                // Check for double step
                ChessPosition twoSteps = new ChessPosition(myPosition.getRow() + moveDirection * 2, myPosition.getColumn());
                if (inBounds(twoSteps) && board.getPiece(twoSteps) == null && myPosition.getRow() == (color == ChessGame.TeamColor.WHITE ? 2 : 7)) {
                    validMoves.add(new ChessMove(myPosition, twoSteps, null));
                }
            }
        }

        // Diagonal captures
        if (myPosition.getColumn() != 8) {
            ChessPosition rightKill = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn() + 1);
            if (inBounds(rightKill) && board.getPiece(rightKill) != null && board.getPiece(rightKill).getTeamColor() != color) {
                if (rightKill.getRow() == 8 && color == ChessGame.TeamColor.WHITE || rightKill.getRow() == 1 && color == ChessGame.TeamColor.BLACK) {
                    // Add all promotion moves
                    addPromotions(validMoves, myPosition, rightKill);
                } else {
                    validMoves.add(new ChessMove(myPosition, rightKill, null));
                }
            }
        }

        if (myPosition.getColumn() != 1) {
            ChessPosition leftKill = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn() - 1);
            if (inBounds(leftKill) && board.getPiece(leftKill) != null && board.getPiece(leftKill).getTeamColor() != color) {
                if (leftKill.getRow() == 8 && color == ChessGame.TeamColor.WHITE || leftKill.getRow() == 1 && color == ChessGame.TeamColor.BLACK) {
                    // Add all promotion moves
                    addPromotions(validMoves, myPosition, leftKill);
                } else {
                    validMoves.add(new ChessMove(myPosition, leftKill, null));
                }
            }
        }
        return validMoves;
    }

    private void addPromotions(List<ChessMove> validMoves, ChessPosition from, ChessPosition to) {
        // Add promotion moves for all possible piece types
        for (ChessPiece.PieceType promotionType : new ChessPiece.PieceType[]{ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT}) {
            validMoves.add(new ChessMove(from, to, promotionType));
        }
    }

    private boolean inBounds(ChessPosition newSpot) {
        // System.out.println("Checking bounds for: (" + newSpot.getRow() + ", " + newSpot.getColumn() + ")");
        return (newSpot.getRow() <= 8 && newSpot.getRow() >= 1 && newSpot.getColumn() >= 1 && newSpot.getColumn() <= 8);
    }

    private void addLinearMoves(List<ChessMove> moves, ChessBoard board, ChessPosition myPosition, int rowStep, int colStep) {
        int row = myPosition.getRow() + rowStep;
        int col = myPosition.getColumn() + colStep;

        while (row >= 1 && row <= 8 && col >= 1 && col <= 8) { // Ensure within bounds
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece blockingPiece = board.getPiece(newPos);

            if (blockingPiece == null) {
                moves.add(new ChessMove(myPosition, newPos, null)); // Valid move
            } else {
                if (blockingPiece.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, newPos, null)); // Valid kill
                }
                break; // Stop movement after hitting a piece
            }

            row += rowStep; // Increment in the given direction
            col += colStep;
        }
    }


    ////////////////////// OVERIDES ////////////////////////

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return type == that.type && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }
}

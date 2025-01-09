package chess;

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

    PieceType type;
    ChessGame.TeamColor color;
    ChessPosition position;

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

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<>();

        switch (type) {
            case PAWN:
                // Calculate pawn moves
                int moveDirection = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;

                ChessPosition oneStep = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn());
                if(board.getPiece(oneStep) == null){
                    validMoves.add(new ChessMove(myPosition, oneStep, null));
                }
                break;
            case ROOK:
                // Calculate rook moves
                break;
            case KNIGHT:
                // Calculate knight moves
                break;
            case BISHOP:
                // Calculate bishop moves
                break;
            case QUEEN:
                // Calculate queen moves
                break;
            case KING:
                // Calculate king moves
                break;
            default:
                throw new RuntimeException("Invalid piece type");
        }
        return null;
    }

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

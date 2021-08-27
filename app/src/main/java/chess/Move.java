package chess;

import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.Objects;

/**
 * This class contains all the information needed to make a move.
 */
public class Move {

    /**
     * The coordinate the piece starts on.
     */
    private final ChessCoordinate startingCoordinate;

    /**
     * The coordinate the piece ends on.
     */
    private final ChessCoordinate endingCoordinate;

    /**
     * The piece that is moving.
     */
    private final Piece movingPiece;

    /**
     * The coordinate the interacting piece starts on.
     */
    private final ChessCoordinate interactingPieceStart;

    /**
     * The coordinate the interacting piece ends on.
     */
    private final ChessCoordinate interactingPieceEnd;

    /**
     * The piece that is interacting with the moving piece. This may be a captured
     * piece or a rook that is castling.
     */
    private final Piece interactingPiece;

    /**
     * The piece the moving piece gets promoted to.
     */
    private final Piece promotedPiece;

    /**
     * Creates a move that does not capture or do anything special.
     *
     * @param endingCoordinate the ending coordinate.
     * @param movingPiece the moving piece.
     */
    public Move(ChessCoordinate endingCoordinate, Piece movingPiece) {
        this(endingCoordinate, movingPiece, null, null, null);
    }

    /**
     * Creates a move that interacts with another piece. This could be a
     * standard capture, or a castling move.
     *
     * @param endingCoordinate the coordinate the moving piece moves to.
     * @param movingPiece the piece that is moved.
     * @param interactingPieceEnd the coordinate the interacting piece moves to.
     * @param interactingPiece the piece the moving piece interacts with.
     */
    public Move(ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceEnd, Piece interactingPiece) {
        this(endingCoordinate, movingPiece, interactingPieceEnd, interactingPiece, null);
    }

    /**
     * Creates a standard promotion move that does not capture.
     *
     * @param endingCoordinate the coordinate the moving piece moves to.
     * @param movingPiece the piece that is moved.
     * @param promotedPiece the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate endingCoordinate, Piece movingPiece, Piece promotedPiece) {
        this(endingCoordinate, movingPiece, null, null, promotedPiece);
    }

    /**
     * Creates a move that interacts with another piece and promotes.
     *
     * @param endingCoordinate the coordinate the moving piece moves to.
     * @param movingPiece the piece that is moved.
     * @param interactingPieceEnd the coordinate the interacting piece moves to.
     * @param interactingPiece the piece the moving piece interacts with.
     * @param promotedPiece the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceEnd, Piece interactingPiece, Piece promotedPiece) {
        this.startingCoordinate = movingPiece.getCoordinate();
        this.endingCoordinate = endingCoordinate;
        this.movingPiece = movingPiece;
        this.interactingPieceStart = interactingPiece == null ? null : interactingPiece.getCoordinate();
        this.interactingPieceEnd = interactingPieceEnd;
        this.interactingPiece = interactingPiece;
        this.promotedPiece = promotedPiece;
    }

    /**
     * @return weather this move results in a promotion.
     */
    public boolean doesPromote() {
        return promotedPiece != null;
    }

    /**
     * @return the starting coordinate of the moving piece
     */
    public ChessCoordinate getStartingCoordinate() {
        return startingCoordinate;
    }

    /**
     * @return the ending coordinate of the moving piece.
     */
    public ChessCoordinate getEndingCoordinate() {
        return endingCoordinate;
    }

    /**
     * @return the moving piece.
     */
    public Piece getMovingPiece() {
        return movingPiece;
    }

    /**
     * @return the starting coordinate of the interacting piece.
     */
    public ChessCoordinate getInteractingPieceStart() {
        return interactingPieceStart;
    }

    /**
     * @return the ending coordinate of the interacting piece.
     */
    public ChessCoordinate getInteractingPieceEnd() {
        return interactingPieceEnd;
    }

    /**
     * @return the interacting piece.
     */
    public Piece getInteractingPiece() {
        return interactingPiece;
    }

    /**
     * @return the piece the moving piece promotes to.
     */
    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (interactingPiece != null && interactingPieceEnd != null) {
            // Castle
            if (endingCoordinate.getFile() == 6) {
                result.append("0-0");
            } else {
                result.append("0-0-0");
            }
        } else {
            // Pawn
            if (movingPiece instanceof Pawn) {
                if (interactingPiece != null) {
                    result.append(startingCoordinate.getCharFile());
                }
            }

            result.append(movingPiece.toString());
            if (interactingPiece != null) {
                result.append("x");
            }
            result.append(endingCoordinate.toString());
        }

        if (doesPromote()) {
            result.append("=");
            result.append(promotedPiece.toString());
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return Objects.equals(startingCoordinate, move.startingCoordinate)
                && Objects.equals(endingCoordinate, move.endingCoordinate)
                && Objects.equals(movingPiece, move.movingPiece)
                && Objects.equals(interactingPieceStart, move.interactingPieceStart)
                && Objects.equals(interactingPieceEnd, move.interactingPieceEnd)
                && Objects.equals(interactingPiece, move.interactingPiece)
                && Objects.equals(promotedPiece, move.promotedPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingCoordinate, endingCoordinate, movingPiece, interactingPieceStart, interactingPieceEnd, interactingPiece, promotedPiece);
    }
}

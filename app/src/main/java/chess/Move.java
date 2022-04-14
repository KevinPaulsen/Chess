package chess;

import chess.model.pieces.Piece;

import java.util.Objects;

import static chess.model.pieces.Piece.BLACK_PAWN;
import static chess.model.pieces.Piece.WHITE_PAWN;

/**
 * This class contains all the information needed to make a move.
 */
public class Move {

    private static int currentMove = 0;

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

    private final int moveNumber;

    /**
     * Creates a move that does not capture or do anything special.
     *
     * @param endingCoordinate the ending coordinate.
     * @param movingPiece      the moving piece.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, Piece movingPiece) {
        this(startingCoordinate, endingCoordinate, movingPiece,
                null, null, null);
    }

    /**
     * Creates a move that interacts with another piece. This could be a
     * standard capture, or a castling move.
     *
     * @param startingCoordinate    the coordinate the moving piece starts on.
     * @param endingCoordinate      the coordinate the moving piece moves to.
     * @param movingPiece           the piece that is moved.
     * @param interactingPieceStart the coordinate the interacting piece starts on.
     * @param interactingPieceEnd   the coordinate the interacting piece moves to.
     * @param interactingPiece      the piece the moving piece interacts with.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceStart, ChessCoordinate interactingPieceEnd, Piece interactingPiece) {
        this(startingCoordinate, endingCoordinate, movingPiece, interactingPieceStart,
                interactingPieceEnd, interactingPiece, null);
    }

    /**
     * Creates a standard promotion move that does not capture.
     *
     * @param endingCoordinate the coordinate the moving piece moves to.
     * @param movingPiece      the piece that is moved.
     * @param promotedPiece    the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, Piece movingPiece, Piece promotedPiece) {
        this(startingCoordinate, endingCoordinate, movingPiece, null, null, null, promotedPiece);
    }

    /**
     * Creates a move that interacts with another piece and promotes.
     *
     * @param endingCoordinate    the coordinate the moving piece moves to.
     * @param movingPiece         the piece that is moved.
     * @param interactingPieceEnd the coordinate the interacting piece moves to.
     * @param interactingPiece    the piece the moving piece interacts with.
     * @param promotedPiece       the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, Piece movingPiece,
                ChessCoordinate interactingPieceStart, ChessCoordinate interactingPieceEnd, Piece interactingPiece,
                Piece promotedPiece) {
        this.startingCoordinate = startingCoordinate;
        this.endingCoordinate = endingCoordinate;
        this.movingPiece = movingPiece;
        this.interactingPieceStart = interactingPiece == null ? null : interactingPieceStart;
        this.interactingPieceEnd = interactingPieceEnd;
        this.interactingPiece = interactingPiece;
        this.promotedPiece = promotedPiece;
        this.moveNumber = currentMove;
        currentMove++;
    }

    /**
     * @return weather this move results in a promotion.
     */
    public boolean doesPromote() {
        return promotedPiece != null;
    }

    public boolean doesCastle() {
        return interactingPiece != null && interactingPieceEnd != null;
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

        if (doesCastle()) {
            // Castle
            if (endingCoordinate.getFile() == 6) {
                result.append("O-O");
            } else {
                result.append("O-O-O");
            }
        } else {
            // Pawn
            if (movingPiece == WHITE_PAWN || movingPiece == BLACK_PAWN) {
                if (interactingPiece != null) {
                    result.append(startingCoordinate.getCharFile());
                }
            } else {
                result.append(movingPiece.getStringRep().toUpperCase());
            }

            if (interactingPiece != null) {
                result.append("x");
            }
            result.append(endingCoordinate.toString());
        }

        if (doesPromote()) {
            result.append("=");
            result.append(promotedPiece.getStringRep().toUpperCase());
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move move)) return false;
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
        return Objects.hash(startingCoordinate, endingCoordinate, movingPiece, interactingPieceStart,
                interactingPieceEnd, interactingPiece, promotedPiece, moveNumber);
    }
}

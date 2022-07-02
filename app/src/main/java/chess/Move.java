package chess;

import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static chess.ChessCoordinate.*;

/**
 * This class contains all the information needed to make a move.
 */
public class Move {

    public static final Move WHITE_CASTLE_KING_SIDE = new Move(E1, G1, H1, F1, new BoardModel("", new Zobrist()));
    public static final Move WHITE_CASTLE_QUEEN_SIDE = new Move(E1, C1, A1, D1, new BoardModel("", new Zobrist()));
    public static final Move BLACK_CASTLE_KING_SIDE = new Move(E8, G8, H8, F8, new BoardModel("", new Zobrist()));
    public static final Move BLACK_CASTLE_QUEEN_SIDE = new Move(E8, C8, A8, D8, new BoardModel("", new Zobrist()));

    /**
     * The coordinate the piece starts on.
     */
    private final ChessCoordinate startingCoordinate;

    /**
     * The coordinate the piece ends on.
     */
    private final ChessCoordinate endingCoordinate;

    /**
     * The coordinate the interacting piece starts on.
     */
    private final ChessCoordinate interactingPieceStart;

    /**
     * The coordinate the interacting piece ends on.
     */
    private final ChessCoordinate interactingPieceEnd;

    /**
     * The piece the moving piece gets promoted to.
     */
    private final Piece promotedPiece;

    private final List<PieceChangeData> moveInstructions;
    private final long occupancyChange;

    /**
     * Creates a move that does not capture or do anything special.
     *
     * @param startingCoordinate    the coordinate the moving piece starts on.
     * @param endingCoordinate the ending coordinate.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, BoardModel board) {
        this(startingCoordinate, endingCoordinate, null, null, null, board);
    }

    /**
     * Creates a move that interacts with another piece. This could be a
     * standard capture, or a castling move.
     *
     * @param startingCoordinate    the coordinate the moving piece starts on.
     * @param endingCoordinate      the coordinate the moving piece moves to.
     * @param interactingPieceStart the coordinate the interacting piece starts on.
     * @param interactingPieceEnd   the coordinate the interacting piece moves to.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate,
                ChessCoordinate interactingPieceStart, ChessCoordinate interactingPieceEnd, BoardModel board) {
        this(startingCoordinate, endingCoordinate, interactingPieceStart, interactingPieceEnd, null, board);
    }

    /**
     * Creates a standard promotion move that does not capture.
     *
     * @param startingCoordinate    the coordinate the moving piece starts on.
     * @param endingCoordinate the coordinate the moving piece moves to.
     * @param promotedPiece    the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate, Piece promotedPiece, BoardModel board) {
        this(startingCoordinate, endingCoordinate, null, null, promotedPiece, board);
    }

    /**
     * Creates a move that interacts with another piece and promotes.
     *
     * @param startingCoordinate    the coordinate the moving piece starts on.
     * @param endingCoordinate    the coordinate the moving piece moves to.
     * @param interactingPieceStart the coordinate the interacting piece starts on.
     * @param interactingPieceEnd the coordinate the interacting piece moves to.
     * @param promotedPiece       the piece the moving piece promotes to.
     */
    public Move(ChessCoordinate startingCoordinate, ChessCoordinate endingCoordinate,
                ChessCoordinate interactingPieceStart, ChessCoordinate interactingPieceEnd, Piece promotedPiece,
                BoardModel board) {
        this.moveInstructions = new ArrayList<>();

        Piece movingPiece = board.getPieceOn(startingCoordinate);
        Piece interactingPiece = board.getPieceOn(endingCoordinate);

        long movingChangeMask = startingCoordinate.getBitMask();
        if (promotedPiece == null) {
            movingChangeMask |= endingCoordinate.getBitMask();
        } else {
            moveInstructions.add(new PieceChangeData(promotedPiece, endingCoordinate.getBitMask()));
        }
        moveInstructions.add(new PieceChangeData(movingPiece, movingChangeMask));
        long occupancyChange = movingChangeMask;

        if (interactingPiece != null) {
            long interactingChangeMask = interactingPieceStart.getBitMask();

            if (interactingPieceEnd != null) {
                interactingChangeMask |= interactingPieceEnd.getBitMask();
            }
            moveInstructions.add(new PieceChangeData(interactingPiece, interactingChangeMask));
            occupancyChange ^= interactingChangeMask;
        }

        this.occupancyChange = occupancyChange;

        this.startingCoordinate = startingCoordinate;
        this.endingCoordinate = endingCoordinate;
        this.interactingPieceStart = interactingPieceStart;
        this.interactingPieceEnd = interactingPieceEnd;
        this.promotedPiece = promotedPiece;
    }

    /**
     * @return weather this move results in a promotion.
     */
    public boolean doesPromote() {
        return promotedPiece != null;
    }

    public boolean doesCastle() {
        return interactingPieceStart != null && interactingPieceEnd != null;
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
    public Piece getMovingPiece(BoardModel board) {
        return board.getPieceOn(startingCoordinate);
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
    public Piece getInteractingPiece(BoardModel board) {
        return board.getPieceOn(interactingPieceStart);
    }

    /**
     * @return the piece the moving piece promotes to.
     */
    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    public List<PieceChangeData> getMoveInstructions() {
        return moveInstructions;
    }

    public long getOccupancyChange() {
        return occupancyChange;
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
            result.append(startingCoordinate);
            result.append(endingCoordinate);
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
                && Objects.equals(interactingPieceStart, move.interactingPieceStart)
                && Objects.equals(interactingPieceEnd, move.interactingPieceEnd)
                && Objects.equals(promotedPiece, move.promotedPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingCoordinate, endingCoordinate, interactingPieceStart,
                interactingPieceEnd, promotedPiece);
    }

    public record PieceChangeData(Piece piece, long changeData) {}
}

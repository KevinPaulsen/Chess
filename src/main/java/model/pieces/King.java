package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This class hold the information
 * for how a King moves. This class can also calculate if a given chessCoordinate
 * is attacked by the opposite color.
 */
public class King extends Piece {

    /**
     * The move maker for a castle move. This MoveMaker ensures that the
     * King does not travel over an attacked coordinate, and isn't in check.
     * It also guarantees that there are no pieces in-between this and the rook,
     * also that neither the rook nor the king have moved. If any of these
     * conditions are false, then null is returned.
     */
    private final MoveMaker castleMoveMaker = (start, end, piece, game) -> {
        if (!hasMoved && (end.getFile() == 2 || end.getFile() == 6)) {
            int direction = end.getFile() == 2 ? -1 : 1;
            BoardModel board = game.getBoard();

            // The coordinate directly next to the king in the direction we are castling.
            ChessCoordinate coordinate1 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + direction, coordinate.getRank());
            // The coordinate 1 space away from the king in the direction we are castling.
            ChessCoordinate coordinate2 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + 2 * direction, coordinate.getRank());
            // The coordinate next to rook on the A file.
            ChessCoordinate coordinate3 = BoardModel.getChessCoordinate(
                    coordinate.getFile() + -3, coordinate.getRank());
            // The rook we are castling with.
            Piece rook = board.getPieceOn(BoardModel.getChessCoordinate(
                    direction == 1 ? 7 : 0, start.getRank()));

            if (rook instanceof Rook && !rook.hasMoved
                    && board.getPieceOn(coordinate1) == null
                    && board.getPieceOn(coordinate2) == null
                    && (direction == 1 || board.getPieceOn(coordinate3) == null)
                    && !isInCheck(coordinate)
                    && !isInCheck(coordinate1)
                    && !isInCheck(coordinate2)) {
                return new Move(end, piece, coordinate1, rook);
            }
        }
        return null;
    };

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    /**
     * Returns the set of movement rules for this King.
     *
     * @return the set of movement rules for this King.
     */
    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, 0), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, -1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 0), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -1), 1, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 2), 1, castleMoveMaker),
                new MovementRule(new Direction(0, -2), 1, castleMoveMaker)
        );
    }

    public boolean isInCheck(ChessCoordinate coordinate) {
        return false;
    }
}

package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.GameModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Bishop.
 */
public class Bishop extends Piece {

    /**
     * Creates a new bishop with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a bishop.
     *
     * @param color the color of this Bishop
     * @param coordinate the coordinate of this Bishop
     */
    public Bishop(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
        movementRules.addAll(getMovementRules());
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public Bishop(Piece piece) {
        super(piece);
        movementRules.addAll(getMovementRules());
    }

    @Override
    protected Move makeMove(GameModel gameModel, ChessCoordinate coordinate) {
        Direction direction = Direction.getNormalDirectionTo(this.coordinate, coordinate);

        if (direction != null && direction.isDiagonal()) {
            STANDARD_MOVE_MAKER.getMove(coordinate, coordinate, gameModel, Pawn.QUEEN_PROMOTION);
        }

        return null;
    }

    /**
     * Returns the movement rules of this Bishop.
     *
     * @return the set of movement rules of this Bishop.
     */
    private static Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.DIAGONALS.directions) {
            movementRules.add(new MovementRule(direction, LONG_MOVING_MAX, STANDARD_MOVE_MAKER));
        }
        return Collections.unmodifiableSet(movementRules);
    }

    @Override
    public String toString() {
        return "B";
    }
}

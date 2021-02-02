package main.java.model.pieces;

import main.java.ChessCoordinate;

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
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    /**
     * Returns the movement rules of this Bishop.
     *
     * @return the set of movement rules of this Bishop.
     */
    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 1), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -1), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 1), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -1), 8, STANDARD_MOVE_MAKER)
        );
    }
}

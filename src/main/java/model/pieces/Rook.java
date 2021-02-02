package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Set;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Rook.
 */
public class Rook extends Piece {

    /**
     * Creates a new Rook with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a rook.
     *
     * @param color the color of this rook
     * @param coordinate the coordinate of this rook
     */
    public Rook(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    /**
     * Returns the movement rules of this Rook.
     *
     * @return the set of movement rules of this Rook.
     */
    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 0), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 0), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 1), 8, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, -1), 8, STANDARD_MOVE_MAKER)
        );
    }
}

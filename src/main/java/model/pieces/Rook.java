package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Collections;
import java.util.HashSet;
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
        super(color, coordinate);
        movementRules = getMovementRules();
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public Rook(Piece piece) {
        super(piece);
        movementRules = getMovementRules();
    }

    /**
     * Returns the movement rules of this Rook.
     *
     * @return the set of movement rules of this Rook.
     */
    private Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.STRAIGHTS.directions) {
            movementRules.add(new MovementRule(direction, LONG_MOVING_MAX, STANDARD_MOVE_MAKER));
        }
        return Collections.unmodifiableSet(movementRules);
    }

    @Override
    public String toString() {
        return "R";
    }
}

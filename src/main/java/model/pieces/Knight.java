package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of Piece. This class implements a Knight Piece that
 * moves how a Knight should.
 */
public class Knight extends Piece {

    /**
     * Creates a Knight with the given color and coordinate.
     *
     * @param color the color of this knight
     * @param coordinate the coordinate of this knight
     */
    public Knight(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
        movementRules = getMovementRules();
        this.pieceValue = 3;
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public Knight(Piece piece) {
        super(piece);
        movementRules = getMovementRules();
        this.pieceValue = 3;
    }

    /**
     * Returns the MovementRules of this Knight
     *
     * @return the MovementRules of this knight
     */
    private Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.KNIGHTS.directions) {
            movementRules.add(new MovementRule(direction, 1, STANDARD_MOVE_MAKER));
        }
        return Collections.unmodifiableSet(movementRules);
    }

    @Override
    public String toString() {
        return "N";
    }
}

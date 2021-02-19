package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.GameModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Queen.
 */
public class Queen extends Piece {

    /**
     * Creates a new Rook with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a rook.
     *
     * @param color the color of this rook
     * @param coordinate the coordinate of this rook
     */
    public Queen(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
        movementRules.addAll(getMovementRules());
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public Queen(Piece piece) {
        super(piece);
        movementRules.addAll(getMovementRules());
    }

    /**
     * If able to move, creates the move to the given coordinate. Assumes that
     * no piece blocks, and isn't pinned.
     *
     * @param gameModel  the game this move occurs in
     * @param coordinate the ending coordinate
     * @return the move that moves from this.coordinate to coordinate
     */
    @Override
    protected Move makeMove(GameModel gameModel, ChessCoordinate coordinate) {
        Direction direction = Direction.getNormalDirectionTo(this.coordinate, coordinate);

        if (direction != null) {
            return STANDARD_MOVE_MAKER.getMove(this.coordinate, coordinate, gameModel, Pawn.QUEEN_PROMOTION);
        }

        return null;
    }

    /**
     * Returns the movement rules of this Queen.
     *
     * @return the set of movement rules of this Queen.
     */
    private Set<MovementRule> getMovementRules() {
        Set<MovementRule> movementRules = new HashSet<>();
        for (Direction direction : Directions.DIAGONALS.directions) {
            movementRules.add(new MovementRule(direction, LONG_MOVING_MAX, STANDARD_MOVE_MAKER));
        }
        for (Direction direction : Directions.STRAIGHTS.directions) {
            movementRules.add(new MovementRule(direction, LONG_MOVING_MAX, STANDARD_MOVE_MAKER));
        }
        return Collections.unmodifiableSet(movementRules);
    }

    @Override
    public String toString() {
        return "Q";
    }
}

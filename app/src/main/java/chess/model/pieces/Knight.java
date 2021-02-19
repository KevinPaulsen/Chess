package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.GameModel;

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
        movementRules.addAll(getMovementRules());
    }

    /**
     * Constructs a piece from the given piece. The UID of this
     * piece will be the same as the piece given.
     *
     * @param piece the piece to create this piece with
     */
    public Knight(Piece piece) {
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
        Direction direction = Direction.getDirectionTo(this.coordinate, coordinate);
        if (direction.isKnightDirection()) {
            return STANDARD_MOVE_MAKER.getMove(this.coordinate, coordinate, gameModel, Pawn.QUEEN_PROMOTION);
        }
        return null;
    }

    /**
     * Returns the MovementRules of this Knight
     *
     * @return the MovementRules of this knight
     */
    private static Set<MovementRule> getMovementRules() {
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

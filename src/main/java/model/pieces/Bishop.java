package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Set;

/**
 * Chess Piece that extends piece that knows how a Bishop is meant to move.
 */
public class Bishop extends Piece {

    public Bishop(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    @Override
    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }

    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -1), 8, color, STANDARD_MOVE_MAKER)
        );
    }
}

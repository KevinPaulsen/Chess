package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Set;

public class Knight extends Piece {

    public Knight(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }

    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(2, 1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(2, -1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, 2), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -2), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 2), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -2), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-2, 1), 1, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-2, -1), 1, color, STANDARD_MOVE_MAKER)
        );
    }
}

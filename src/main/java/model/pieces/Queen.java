package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Set;

public class Queen extends Piece {

    public Queen(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }

    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, 0), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(1, -1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 0), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, -1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, -1), 8, color, STANDARD_MOVE_MAKER)
        );
    }
}

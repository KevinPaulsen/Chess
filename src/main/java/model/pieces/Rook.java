package main.java.model.pieces;

import main.java.ChessCoordinate;

import java.util.Set;

public class Rook extends Piece {

    public Rook(char color, ChessCoordinate coordinate) {
        super(coordinate, color);
        movementRules = getMovementRules();
    }

    @Override
    public boolean canMoveTo(ChessCoordinate endCoordinate, Piece[][] pieceArray) {
        return false;
    }

    private Set<MovementRule> getMovementRules() {
        return Set.of(
                new MovementRule(new Direction(1, 0), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(-1, 0), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, 1), 8, color, STANDARD_MOVE_MAKER),
                new MovementRule(new Direction(0, -1), 8, color, STANDARD_MOVE_MAKER)
        );
    }
}

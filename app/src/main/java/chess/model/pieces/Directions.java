package chess.model.pieces;

import chess.ChessCoordinate;

import static chess.model.pieces.Direction.*;

public class Directions {

    public static final Direction[] DIAGONALS =
            new Direction[]{UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    public static final Direction[] DIAGONAL_COMPLEMENTS = new Direction[]{UP_LEFT, UP_RIGHT};
    public static final Direction[] STRAIGHT_COMPLEMENTS = new Direction[]{UP, RIGHT};
    public static final Direction[] STRAIGHTS = new Direction[]{LEFT, RIGHT, UP, DOWN};
    public static final Direction[] KNIGHTS = new Direction[]{
            UP_UP_RIGHT, UP_UP_LEFT, UP_LEFT_LEFT, DOWN_LEFT_LEFT, DOWN_DOWN_LEFT, DOWN_DOWN_RIGHT,
            DOWN_RIGHT_RIGHT, UP_RIGHT_RIGHT
    };
    public static final Direction[] VERTICAL = new Direction[]{UP, DOWN};
    public static final Direction[] LATERAL = new Direction[]{RIGHT, LEFT};
    public static final Direction[] ALL_DIRECTIONS =
            new Direction[]{UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT};

    public static boolean areAligned(ChessCoordinate c1, ChessCoordinate c2) {
        return areDiagonallyAligned(c1, c2) || areStraightlyAligned(c1, c2);
    }

    public static boolean areDiagonallyAligned(ChessCoordinate c1, ChessCoordinate c2) {
        return Math.abs(c1.getRank() - c2.getRank()) == Math.abs(c1.getFile() - c2.getFile());
    }

    public static boolean areStraightlyAligned(ChessCoordinate c1, ChessCoordinate c2) {
        return c1.getFile() == c2.getFile() || c1.getRank() == c2.getRank();
    }
}

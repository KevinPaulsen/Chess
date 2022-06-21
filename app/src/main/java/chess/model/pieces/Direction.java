package chess.model.pieces;

import chess.ChessCoordinate;

/**
 * This class stores a direction, and can calculate if
 * two ChessCoordinates are aligned.
 */
public enum Direction {

    // Straights
    UP(1, 0),
    DOWN(-1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),

    // Diagonals
    UP_RIGHT(1, 1),
    UP_LEFT(1, -1),
    DOWN_RIGHT(-1, 1),
    DOWN_LEFT(-1, -1),

    // Knights
    UP_UP_RIGHT(2, 1),
    UP_UP_LEFT(2, -1),
    UP_LEFT_LEFT(1, -2),
    UP_RIGHT_RIGHT(1, 2),
    DOWN_LEFT_LEFT(-1, -2),
    DOWN_RIGHT_RIGHT(-1, 2),
    DOWN_DOWN_LEFT(-2, -1),
    DOWN_DOWN_RIGHT(-2, 1);

    final int rise;
    final int run;

    /**
     * Creates a new Direction with the given rise and run.
     *
     * @param rise the vertical increase
     * @param run  the horizontal increase.
     */
    Direction(int rise, int run) {
        this.rise = rise;
        this.run = run;
    }

    /**
     * Returns the coordinate after the given coordinate and in this Direction.
     * If coordinate is null, null is returned. If the next coordinate is out of
     * Bounds, null is returned.
     *
     * @param coordinate the starting coordinate.
     * @return the coordinate after the given coordinate and in this Direction.
     */
    public ChessCoordinate next(ChessCoordinate coordinate) {
        ChessCoordinate next = null;
        if (coordinate != null) {
            int file = coordinate.getFile() + run;
            int rank = coordinate.getRank() + rise;
            if (ChessCoordinate.isInBounds(file, rank)) {
                next = ChessCoordinate.getChessCoordinate(file, rank);
            }
        }
        return next;
    }
}


package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;

import java.util.Objects;

/**
 * This class stores a direction, and can calculate if
 * two ChessCoordinates are aligned.
 */
public class Direction {

    // The vertical increase
    private final int rise;
    // The horizontal increase
    private final int run;

    /**
     * Creates a new Direction with the given rise and run.
     *
     * @param rise the vertical increase
     * @param run  the horizontal increase.
     */
    public Direction(int rise, int run) {
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
            next = BoardModel.getChessCoordinate(coordinate.getFile() + run, coordinate.getRank() + rise);
        }
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direction)) return false;
        Direction direction = (Direction) o;
        return rise == direction.rise && run == direction.run;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rise, run);
    }
}


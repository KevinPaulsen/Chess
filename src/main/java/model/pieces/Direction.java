package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;

import java.util.Objects;

/**
 * This class stores a direction, and can calculate if
 * two ChessCoordinates are aligned.
 */
public class Direction {

    private final int rise;
    private final int run;
    private final double slope;

    public Direction(int rise, int run) {
        this.rise = rise;
        this.run = run;
        slope = calculateSlope(rise, run);
    }

    /**
     * Can check if two coordinates are aligned. Two coordinates are
     * considered aligned if the slope between startCoordinate and endCoordinate
     * are equal to this slope.
     *
     * @param startCoordinate the first coordinate
     * @param endCoordinate the second coordinate
     * @return if they two coordinates are aligned in this direction.
     */
    public boolean areAligned(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        int rise = startCoordinate.getRank() - endCoordinate.getRank();
        int run = startCoordinate.getFile() - endCoordinate.getFile();
        return slope == calculateSlope(rise, run);
    }

    public ChessCoordinate next(ChessCoordinate coordinate) {
        ChessCoordinate next = null;
        if (coordinate != null) {
            next = BoardModel.getChessCoordinate(coordinate.getFile() + run, coordinate.getRank() + rise);
        }
        return next;
    }


    /**
     * Calculates the slope given two integers.
     *
     * @param rise the vertical aspect.
     * @param run the horizontal aspect.
     * @return the slope.
     */
    private static double calculateSlope(int rise, int run) {
        return ((double) rise) / run;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Direction)) return false;
        Direction direction = (Direction) o;
        return rise == direction.rise && run == direction.run && Double.compare(direction.slope, slope) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rise, run, slope);
    }
}

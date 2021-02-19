package chess.model.pieces;

import chess.ChessCoordinate;
import chess.model.BoardModel;

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
            int file = coordinate.getFile() + run;
            int rank = coordinate.getRank() + rise;
            if (ChessCoordinate.isInBounds(file, rank)) {
                next = BoardModel.getChessCoordinate(file, rank);
            }
        }
        return next;
    }

    private static int scaleToOne(int num) {
        return num == 0 ? 0 : num / Math.abs(num);
    }

    public Direction getOpposite() {
        return new Direction(-rise, -run);
    }

    public static Direction getDirectionTo(ChessCoordinate start, ChessCoordinate end) {
        int riseOffset = end.getRank() - start.getRank();
        int runOffset = end.getFile() - start.getFile();
        return new Direction(riseOffset, runOffset);
    }

    /**
     * If the two directions are axially aligned or diagonally aligned, a direction
     * between the two is returned. Null otherwise.
     *
     * @param start the starting coordinate
     * @param end the ending coordinate
     * @return the direction between the two
     */
    public static Direction getNormalDirectionTo(ChessCoordinate start, ChessCoordinate end) {
        int riseOffset = end.getRank() - start.getRank();
        int runOffset = end.getFile() - start.getFile();

        if (riseOffset == 0 || runOffset == 0 || Math.abs(riseOffset) == Math.abs(runOffset)) {
            return new Direction(scaleToOne(riseOffset), scaleToOne(runOffset));
        }
        return null;
    }

    public static boolean areMajorlyAligned(ChessCoordinate start, ChessCoordinate end) {
        int riseOffset = end.getRank() - start.getRank();
        int runOffset = end.getFile() - start.getFile();
        double slope = runOffset == 0 ? 0 : ((double) riseOffset) / runOffset;
        return slope == 0 || Math.abs(slope) == 1;
    }

    public boolean isDiagonal() {
        return rise * run != 0;
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

    public boolean isKnightDirection() {
        return Math.abs(rise) + Math.abs(run) == 3 && !isAxial();
    }

    public boolean isAxial() {
        return rise * run == 0;
    }
}


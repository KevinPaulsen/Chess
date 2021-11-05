package chess.model.pieces;

import chess.ChessCoordinate;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Bishop.
 */
public class Bishop extends Piece {

    /**
     * The map of reachable coordinates bishops can get to from each position on the board.
     */
    public static final List<List<ChessCoordinate>>[][] REACHABLE_COORDINATES_MAP =
            generateReachableCoordinates(Bishop::generateReachableCoordsAt);

    /**
     * Creates a new bishop with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a bishop.
     *
     * @param color the color of this Bishop
     */
    public Bishop(char color) {
        super(Bishop::generateReachableCoordsAt, color);
    }

    /**
     * Creates a Bishop from a pawn. This is used for promotion. The UID and
     * all other properties of this piece are the exact same as the given pawn.
     *
     * @param pawn the pawn that is promoted.
     */
    public Bishop(Pawn pawn) {
        super(pawn, Bishop::generateReachableCoordsAt);
    }

    @Override
    public String toString() {
        return "B";
    }

    private static List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.DIAGONALS.directions) {
            List<ChessCoordinate> ray = new ArrayList<>();
            for (ChessCoordinate currentCoord = direction.next(coordinate);
                 currentCoord != null;
                 currentCoord = direction.next(currentCoord)) {
                ray.add(currentCoord);
            }
            result.add(ImmutableList.copyOf(ray));
        }

        return ImmutableList.copyOf(result);
    }
}

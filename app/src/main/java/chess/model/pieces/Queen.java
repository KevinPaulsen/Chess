package chess.model.pieces;

import chess.ChessCoordinate;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This implementation specifies the
 * movement and movement of a Queen.
 */
public class Queen extends Piece {

    /**
     * The map of reachable coordinates Queens can get to from each position on the board.
     */
    public static final List<List<ChessCoordinate>>[][] REACHABLE_COORDINATES_MAP =
            generateReachableCoordinates(Queen::generateReachableCoordsAt);

    /**
     * Creates a new Rook with the given color and placed on the given
     * coordinate. This sets the Movement rules to the movement rules of
     * a rook.
     *
     * @param color the color of this rook
     */
    public Queen(char color) {
        super(Queen::generateReachableCoordsAt, color);
    }

    /**
     * Creates a Queen from a pawn. This is used for promotion. The UID and
     * all other properties of this piece are the exact same as the given pawn.
     *
     * @param pawn the pawn that is promoted.
     */
    public Queen(Pawn pawn) {
        super(pawn, Queen::generateReachableCoordsAt);
    }

    @Override
    public String toString() {
        return "Q";
    }

    private static List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.ALL_DIRECTIONS.directions) {
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

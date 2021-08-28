package chess.model.pieces;

import chess.ChessCoordinate;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This class implements a Knight Piece that
 * moves how a Knight should.
 */
public class Knight extends Piece {

    /**
     * Creates a Knight with the given color and coordinate.
     *
     * @param color the color of this knight
     * @param coordinate the coordinate of this knight
     */
    public Knight(char color, ChessCoordinate coordinate) {
        super(generateReachableCoordinates(Knight::generateReachableCoordsAt), color, coordinate);
    }

    /**
     * Creates a Knight from a pawn. This is used for promotion. The UID and
     * all other properties of this piece are the exact same as the given pawn.
     *
     * @param pawn the pawn that is promoted.
     */
    public Knight(Pawn pawn) {
        super(pawn, generateReachableCoordinates(Knight::generateReachableCoordsAt));
    }

    @Override
    public String toString() {
        return "N";
    }

    private static List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.KNIGHTS.directions) {
            ChessCoordinate nextCoord = direction.next(coordinate);
            result.add(nextCoord == null ? List.of() : List.of(nextCoord));
        }

        return ImmutableList.copyOf(result);
    }
}

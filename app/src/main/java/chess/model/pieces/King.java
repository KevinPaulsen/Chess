package chess.model.pieces;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This class hold the information
 * for how a King moves. This class can also calculate if a given chessCoordinate
 * is attacked by the opposite color.
 */
public class King extends Piece {

    /**
     * The map of reachable coordinates Kings can get to from each position on the board.
     */
    public static final List<List<ChessCoordinate>>[][] REACHABLE_COORDINATES_MAP =
            generateReachableCoordinates(King::generateReachableCoordsAt);

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     */
    public King(char color) {
        super(King::generateReachableCoordsAt, color);
    }

    @Override
    public String toString() {
        return "K";
    }

    /**
     * This method will return a List of Lists of ChessCoordinates. The second list
     * will always be exactly 10 elements long. The fist 8 are the 8 directions the
     * king can move. The 9th, is castling King-side, the 10th is castling Queen-side.
     *
     * @param coordinate the coordinate the king is at.
     * @return list of lists of ChessCoordinates that represent the squares the king can move.
     */
    @SuppressWarnings("ConstantConditions")
    private static List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.ALL_DIRECTIONS.directions) {
            ChessCoordinate nextCoord = direction.next(coordinate);
            result.add(nextCoord == null ? List.of() : List.of(nextCoord));
        }

        // If on castling square
        if (coordinate.getFile() == 4 && coordinate.getRank() % 7 == 0) {
            result.add(List.of(BoardModel.getChessCoordinate(coordinate.getFile() + 2, coordinate.getRank())));
            result.add(List.of(BoardModel.getChessCoordinate(coordinate.getFile() - 2, coordinate.getRank())));
        } else {
            result.add(List.of());
            result.add(List.of());
        }

        return ImmutableList.copyOf(result);
    }
}

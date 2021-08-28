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
     * Reference to right direction
     */
    private static final Direction RIGHT = new Direction(0, 1);

    /**
     * Reference to right direction
     */
    private static final Direction LEFT = new Direction(0, -1);

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(generateReachableCoordinates(King::generateReachableCoordsAt), color, coordinate);
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
        }

        return ImmutableList.copyOf(result);
    }
}

package chess.model.pieces;

import chess.ChessCoordinate;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This class contains all
 * movement information of a black pawn.
 */
public class BlackPawn extends Pawn {

    /**
     * The map of reachable coordinates black pawns can get to from each position on the board.
     */
    public static final List<List<ChessCoordinate>>[][] REACHABLE_COORDINATES_MAP =
            generateReachableCoordinates(BlackPawn::generateReachableCoordsAt);

    /**
     * Creates a pawn with the given color and coordinate
     *
     * @param color      the color of this pawn
     * @param coordinate the coordinate of this pawn
     */
    public BlackPawn(char color, ChessCoordinate coordinate) {
        super(REACHABLE_COORDINATES_MAP, color, coordinate);
    }

    /**
     * This method will return a List of Lists of ChessCoordinates. The outer List
     * will always be exactly 3 elements long. The fist one is the straight moving squares
     * the pawn can move to. The second is the right attacking, and the third is the left
     * attacking list.
     *
     * @param coordinate the coordinate the pawn is at.
     * @return list of lists of ChessCoordinates that represent the squares the pawn can move.
     */
    private static List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        List<ChessCoordinate> straightMoves = new ArrayList<>();
        if (coordinate.getRank() > 0) {
            // Add the Straight Moves
            straightMoves.add(Directions.DOWN.next(coordinate));
            if (coordinate.getRank() == 6) {
                straightMoves.add(Directions.DOWN.next(straightMoves.get(0)));
            }

            // Add the left diagonal
            if (coordinate.getFile() > 0) {
                result.add(List.of(Directions.DOWN_LEFT.next(coordinate)));
            } else {
                result.add(List.of());
            }

            // Add the right diagonal
            if (coordinate.getFile() < 7) {
                result.add(List.of(Directions.DOWN_RIGHT.next(coordinate)));
            } else {
                result.add(List.of());
            }
        } else {
            result.add(List.of());
            result.add(List.of());
        }
        result.add(0, ImmutableList.copyOf(straightMoves));


        return ImmutableList.copyOf(result);
    }
}

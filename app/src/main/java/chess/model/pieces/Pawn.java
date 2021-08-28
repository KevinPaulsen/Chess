package chess.model.pieces;

import chess.ChessCoordinate;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of Piece. This class contains all
 * movement information of a pawn.
 */
public abstract class Pawn extends Piece {

    /**
     * Constructs a new Piece with the given color and on the given coordinate.
     *
     * @param reachableCoordinatesMap the map of reachable coordinates.
     * @param color                   the color of this Piece.
     * @param coordinate              the coordinate of this Piece.
     */
    public Pawn(List<List<ChessCoordinate>>[][] reachableCoordinatesMap, char color, ChessCoordinate coordinate) {
        super(reachableCoordinatesMap, color, coordinate);
    }
}

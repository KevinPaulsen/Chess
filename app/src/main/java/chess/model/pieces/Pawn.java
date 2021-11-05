package chess.model.pieces;

import chess.ChessCoordinate;

import java.util.List;

/**
 * This is an implementation of Piece. This class contains all
 * movement information of a pawn.
 */
public abstract class Pawn extends Piece {

    /**
     * Constructs a new Piece with the given color and on the given coordinate.
     *
     * @param mapMaker   the function that makes the map.
     * @param color      the color of this Piece.
     */
    public Pawn(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color) {
        super(mapMaker, color);
    }

    @Override
    public String toString() {
        return "";
    }
}

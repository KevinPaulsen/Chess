package chess.model.pieces;

import chess.ChessCoordinate;

import java.util.List;

/**
 * This represents a map of all the squares a piece can move to from every
 * position on the board.
 */
public class ReachableCoordinatesMap {

    /**
     * The map of coordinates a piece can move to from every position on the map
     */
    private final List<List<ChessCoordinate>>[] reachableCoordinatesMap;

    /**
     * Constructs a new coordinate from the given mapMaker.
     *
     * @param mapMaker this makes the map that this class represents.
     */
    public ReachableCoordinatesMap(CoordinateMapMaker mapMaker) {
        reachableCoordinatesMap = ReachableCoordinatesMap.generateReachableCoordinates(mapMaker);
    }

    @SuppressWarnings("unchecked")
    private static List<List<ChessCoordinate>>[] generateReachableCoordinates(
            CoordinateMapMaker mapMaker) {
        List<List<ChessCoordinate>>[] result = new List[64];

        if (mapMaker != null) {
            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    result[rank * 8 + file] =
                            mapMaker.makeMap(ChessCoordinate.getChessCoordinate(file, rank));
                }
            }
        }

        return result;
    }

    /**
     * Gets the list of coordinates reachable from a given coordinate
     *
     * @param coordinate the coordinate the piece starts on.
     * @return the list of coordinates reachable from a given coordinate.
     */
    public List<List<ChessCoordinate>> getReachableCoordinatesFrom(ChessCoordinate coordinate) {
        return reachableCoordinatesMap[coordinate.getOndDimIndex()];
    }

    interface CoordinateMapMaker {
        List<List<ChessCoordinate>> makeMap(ChessCoordinate coordinate);
    }
}

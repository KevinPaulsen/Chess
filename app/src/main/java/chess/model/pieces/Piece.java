package chess.model.pieces;

import chess.ChessCoordinate;
import chess.model.BoardModel;

import java.util.List;
import java.util.Objects;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public abstract class Piece {

    /**
     * The static count of how many pieces have been created. This
     * is used to uniquely identify each piece.
     */
    private static int PIECE_COUNT = 0;

    private final ReachableCoordinatesMap reachableCoordinatesMap;

    /**
     * The color of this Piece
     */
    protected final char color;

    /**
     * The unique ID of this piece.
     */
    protected final int uid;

    /**
     * Constructs a new Piece with the given color and on the given coordinate.
     *
     * @param mapMaker the function that makes the map.
     * @param color the color of this Piece.
     */
    protected Piece(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color) {
        this(mapMaker, color, PIECE_COUNT);
        PIECE_COUNT++;
    }

    /**
     * Creates a piece from a pawn. This piece has the same UID as the pawn. This
     * should only be used for promotion.
     *
     * @param pawn the pawn that is being promoted.
     * @param mapMaker the function that makes the map.
     */
    protected Piece(Pawn pawn, ReachableCoordinatesMap.CoordinateMapMaker mapMaker) {
        this(mapMaker, pawn.getColor(), pawn.uid);
    }

    /**
     * Constructs a new piece with the given information.
     *
     * @param mapMaker the function that makes the map.
     * @param color the color of this piece.
     * @param uid the unique ID if this piece.
     */
    protected Piece(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color, int uid) {
        this.reachableCoordinatesMap = new ReachableCoordinatesMap(mapMaker);
        this.color = color;
        this.uid = uid;
    }

    /**
     * Returns a list of arrays that represent all the final coordinates that
     * can be reached from this coordinate. The exact representation of this List
     * may vary from implementation to implementation.
     *
     * @return the list of arrays which are all the final coordinates that can be reached from this position.
     */
    public List<List<ChessCoordinate>> getFinalCoordinates(ChessCoordinate coordinate) {
        return reachableCoordinatesMap.getReachableCoordinatesFrom(coordinate);
    }

    /**
     * @return the color of this piece
     */
    public char getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece)) return false;
        Piece piece = (Piece) o;
        return uid == piece.uid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

    @SuppressWarnings("unchecked")
    protected static List<List<ChessCoordinate>>[][] generateReachableCoordinates(ReachableCoordGenerable generable) {
        List<List<ChessCoordinate>>[][] result = new List[8][8];

        for (int file = 0; file < result.length; file++) {
            for (int rank = 0; rank < result[0].length; rank++) {
                result[file][rank] = generable.generateReachableCoordsAt(BoardModel.getChessCoordinate(file, rank));
            }
        }

        return result;
    }

    protected interface ReachableCoordGenerable {
        List<List<ChessCoordinate>> generateReachableCoordsAt(ChessCoordinate coordinate);
    }
}

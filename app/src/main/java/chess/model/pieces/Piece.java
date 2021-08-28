package chess.model.pieces;

import chess.ChessCoordinate;
import chess.model.BoardModel;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    private final List<List<ChessCoordinate>>[][] reachableCoordinatesMap;

    /**
     * The coordinate this piece is at
     */
    protected ChessCoordinate coordinate;

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
     * @param reachableCoordinatesMap the map of reachable coordinates.
     * @param color the color of this Piece.
     * @param coordinate the coordinate of this Piece.
     */
    protected Piece(List<List<ChessCoordinate>>[][] reachableCoordinatesMap, char color, ChessCoordinate coordinate) {
        this(reachableCoordinatesMap, color, coordinate, PIECE_COUNT);
        PIECE_COUNT++;
    }

    /**
     * Creates a piece from a pawn. This piece has the same UID as the pawn. This
     * should only be used for promotion.
     *
     * @param reachableCoordinatesMap the map of reachable coordinates.
     * @param pawn the pawn that is being promoted.
     */
    protected Piece(Pawn pawn, List<List<ChessCoordinate>>[][] reachableCoordinatesMap) {
        this(reachableCoordinatesMap, pawn.getColor(), pawn.coordinate, pawn.uid);
    }

    /**
     * Constructs a new piece with the given information.
     *
     * @param reachableCoordinatesMap the map of reachable coordinates.
     * @param color the color of this piece.
     * @param coordinate the coordinate of this piece.
     * @param uid the unique ID if this piece.
     */
    protected Piece(List<List<ChessCoordinate>>[][] reachableCoordinatesMap, char color,
                    ChessCoordinate coordinate, int uid) {
        this.reachableCoordinatesMap = reachableCoordinatesMap;
        this.color = color;
        this.coordinate = coordinate;
        this.uid = uid;
    }

    /**
     * Returns a list of arrays that represent all the final coordinates that
     * can be reached from this coordinate. The exact representation of this List
     * may vary from implementation to implementation.
     *
     * @return the list of arrays which are all the final coordinates that can be reached from this position.
     */
    public List<List<ChessCoordinate>> getFinalCoordinates() {
        return reachableCoordinatesMap[coordinate.getFile()][coordinate.getRank()];
    }

    /**
     * @return the color of this piece
     */
    public char getColor() {
        return color;
    }

    public void moveTo(ChessCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    /**
     * @return the coordinate this piece is on
     */
    public ChessCoordinate getCoordinate() {
        return coordinate;
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

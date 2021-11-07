package chess.model.pieces;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public enum Piece {
    WHITE_KING(Piece::generateKingReachableCoordsAt, 'w', "K"),
    WHITE_QUEEN(Piece::generateQueenReachableCoordsAt, 'w', "Q"),
    WHITE_ROOK(Piece::generateRookReachableCoordsAt, 'w', "R"),
    WHITE_BISHOP(Piece::generateBishopReachableCoordsAt, 'w', "B"),
    WHITE_KNIGHT(Piece::generateKnightReachableCoordsAt, 'w', "N"),
    WHITE_PAWN(Piece::generateWhitePawnReachableCoordsAt, 'w', ""),

    BLACK_KING(Piece::generateKingReachableCoordsAt, 'b', "k"),
    BLACK_QUEEN(Piece::generateQueenReachableCoordsAt, 'b', "q"),
    BLACK_ROOK(Piece::generateRookReachableCoordsAt, 'b', "r"),
    BLACK_BISHOP(Piece::generateBishopReachableCoordsAt, 'b', "b"),
    BLACK_KNIGHT(Piece::generateKnightReachableCoordsAt, 'b', "n"),
    BLACK_PAWN(Piece::generateBlackPawnReachableCoordsAt, 'b', "");

    /**
     * The map of reachable coordinates
     */
    private final ReachableCoordinatesMap reachableCoordinateMap;

    /**
     * The color of this piece
     */
    private final char color;

    /**
     * The string that this piece should be represented as.
     */
    private final String stringRep;

    /**
     * Construct a new piece with the given reachableCoordinate map.
     *
     * @param mapMaker the mapMaker for this piece.
     */
    Piece(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color, String stringRep) {
        this.reachableCoordinateMap = new ReachableCoordinatesMap(mapMaker);
        this.color = color;
        this.stringRep = stringRep;
    }

    /**
     * @return the reachableCoordinateMap of this piece.
     */
    public List<List<ChessCoordinate>> getReachableCoordinateMapFrom(ChessCoordinate coordinate) {
        return reachableCoordinateMap.getReachableCoordinatesFrom(coordinate);
    }

    /**
     * @return the color of this piece.
     */
    public char getColor() {
        return color;
    }

    /**
     * @return the string representation of this piece.
     */
    public String getStringRep() {
        return stringRep;
    }

    private static List<List<ChessCoordinate>> generateQueenReachableCoordsAt(ChessCoordinate coordinate) {
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

    /**
     * This method will return a List of Lists of ChessCoordinates. The second list
     * will always be exactly 10 elements long. The fist 8 are the 8 directions the
     * king can move. The 9th, is castling King-side, the 10th is castling Queen-side.
     *
     * @param coordinate the coordinate the king is at.
     * @return list of lists of ChessCoordinates that represent the squares the king can move.
     */
    @SuppressWarnings("ConstantConditions")
    private static List<List<ChessCoordinate>> generateKingReachableCoordsAt(ChessCoordinate coordinate) {
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

    private static List<List<ChessCoordinate>> generateRookReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.STRAIGHTS.directions) {
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

    private static List<List<ChessCoordinate>> generateBishopReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.DIAGONALS.directions) {
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

    private static List<List<ChessCoordinate>> generateKnightReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.KNIGHTS.directions) {
            ChessCoordinate nextCoord = direction.next(coordinate);
            result.add(nextCoord == null ? List.of() : List.of(nextCoord));
        }

        return ImmutableList.copyOf(result);
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
    private static List<List<ChessCoordinate>> generateWhitePawnReachableCoordsAt(ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        List<ChessCoordinate> straightMoves = new ArrayList<>();
        if (coordinate.getRank() < 7) {
            // Add the Straight Moves
            straightMoves.add(Directions.UP.next(coordinate));
            if (coordinate.getRank() == 1) {
                straightMoves.add(Directions.UP.next(straightMoves.get(0)));
            }

            // Add the right diagonal
            if (coordinate.getFile() < 7) {
                result.add(List.of(Directions.UP_RIGHT.next(coordinate)));
            } else {
                result.add(List.of());
            }

            // Add the left diagonal
            if (coordinate.getFile() > 0) {
                result.add(List.of(Directions.UP_LEFT.next(coordinate)));
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

    /**
     * This method will return a List of Lists of ChessCoordinates. The outer List
     * will always be exactly 3 elements long. The fist one is the straight moving squares
     * the pawn can move to. The second is the right attacking, and the third is the left
     * attacking list.
     *
     * @param coordinate the coordinate the pawn is at.
     * @return list of lists of ChessCoordinates that represent the squares the pawn can move.
     */
    private static List<List<ChessCoordinate>> generateBlackPawnReachableCoordsAt(ChessCoordinate coordinate) {
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

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
    EMPTY(null, 'w', "", 0),

    WHITE_KING(Piece::generateKingReachableCoordsAt, 'w', "K", 1),
    WHITE_QUEEN(Piece::generateQueenReachableCoordsAt, 'w', "Q", 2),
    WHITE_ROOK(Piece::generateRookReachableCoordsAt, 'w', "R", 3),
    WHITE_BISHOP(Piece::generateBishopReachableCoordsAt, 'w', "B", 4),
    WHITE_KNIGHT(Piece::generateKnightReachableCoordsAt, 'w', "N", 5),
    WHITE_PAWN(Piece::generateWhitePawnReachableCoordsAt, 'w', "P", 6),

    BLACK_KING(Piece::generateKingReachableCoordsAt, 'b', "k", 7),
    BLACK_QUEEN(Piece::generateQueenReachableCoordsAt, 'b', "q", 8),
    BLACK_ROOK(Piece::generateRookReachableCoordsAt, 'b', "r", 9),
    BLACK_BISHOP(Piece::generateBishopReachableCoordsAt, 'b', "b", 10),
    BLACK_KNIGHT(Piece::generateKnightReachableCoordsAt, 'b', "n", 11),
    BLACK_PAWN(Piece::generateBlackPawnReachableCoordsAt, 'b', "p", 12);

    /**
     * The map of reachable coordinates
     */
    private final ReachableCoordinatesMap reachableCoordinateMap;

    /**
     * The color of this piece
     */
    private final char color;

    /**
     * The string that this piece should be represented as in a FEN string.
     */
    private final String stringRep;

    /**
     * The index of this piece.
     */
    private final int uniqueIdx;

    /**
     * Construct a new piece with the given reachableCoordinate map.
     *
     * @param mapMaker the mapMaker for this piece.
     */
    Piece(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color, String stringRep, int uniqueIdx) {
        this.reachableCoordinateMap = new ReachableCoordinatesMap(mapMaker);
        this.color = color;
        this.stringRep = stringRep;
        this.uniqueIdx = uniqueIdx;
    }

    public static Piece getPieceFrom(int uniqueIdx) {
        switch (uniqueIdx) {
            case 0:
                return WHITE_KING;
            case 1:
                return WHITE_QUEEN;
            case 2:
                return WHITE_ROOK;
            case 3:
                return WHITE_BISHOP;
            case 4:
                return WHITE_KNIGHT;
            case 5:
                return WHITE_PAWN;
            case 6:
                return BLACK_KING;
            case 7:
                return BLACK_QUEEN;
            case 8:
                return BLACK_ROOK;
            case 9:
                return BLACK_BISHOP;
            case 10:
                return BLACK_KNIGHT;
            case 11:
                return BLACK_PAWN;
            default:
                return null;
        }
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

    public int getUniqueIdx() {
        return uniqueIdx;
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

            // Add the right diagonal
            if (coordinate.getFile() < 7) {
                result.add(List.of(Directions.DOWN_RIGHT.next(coordinate)));
            } else {
                result.add(List.of());
            }

            // Add the left diagonal
            if (coordinate.getFile() > 0) {
                result.add(List.of(Directions.DOWN_LEFT.next(coordinate)));
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

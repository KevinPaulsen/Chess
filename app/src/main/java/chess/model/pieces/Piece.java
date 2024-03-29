package chess.model.pieces;

import chess.ChessCoordinate;

import java.util.ArrayList;
import java.util.List;

import static chess.model.pieces.Direction.*;
import static chess.model.pieces.Directions.ALL_DIRECTIONS;

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
    Piece(ReachableCoordinatesMap.CoordinateMapMaker mapMaker, char color, String stringRep,
          int uniqueIdx) {
        this.reachableCoordinateMap = new ReachableCoordinatesMap(mapMaker);
        this.color = color;
        this.stringRep = stringRep;
        this.uniqueIdx = uniqueIdx;
    }

    private static List<List<ChessCoordinate>> generateQueenReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : ALL_DIRECTIONS) {
            List<ChessCoordinate> ray = new ArrayList<>();
            for (ChessCoordinate currentCoord = direction.next(coordinate); currentCoord != null;
                 currentCoord = direction.next(currentCoord)) {
                ray.add(currentCoord);
            }
            result.add(ray);
        }

        return result;
    }

    public static Piece getPiece(int uniqueIdx) {
        return switch (uniqueIdx) {
            case 0 -> EMPTY;
            case 1 -> WHITE_KING;
            case 2 -> WHITE_QUEEN;
            case 3 -> WHITE_ROOK;
            case 4 -> WHITE_BISHOP;
            case 5 -> WHITE_KNIGHT;
            case 6 -> WHITE_PAWN;
            case 7 -> BLACK_KING;
            case 8 -> BLACK_QUEEN;
            case 9 -> BLACK_ROOK;
            case 10 -> BLACK_BISHOP;
            case 11 -> BLACK_KNIGHT;
            case 12 -> BLACK_PAWN;
            default -> throw new IllegalArgumentException();
        };
    }

    /**
     * This method will return a List of Lists of ChessCoordinates. The second list
     * will always be exactly 10 elements long. The fist 8 are the 8 directions the
     * king can move. The 9th, is castling King-side, the 10th is castling Queen-side.
     *
     * @param coordinate the coordinate the king is at.
     * @return list of lists of ChessCoordinates that represent the squares the king can move.
     */
    private static List<List<ChessCoordinate>> generateKingReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : ALL_DIRECTIONS) {
            ChessCoordinate nextCoord = direction.next(coordinate);
            result.add(nextCoord == null ? List.of() : List.of(nextCoord));
        }

        // If on castling square
        if (coordinate.getFile() == 4 && coordinate.getRank() % 7 == 0) {
            result.add(List.of(ChessCoordinate.getChessCoordinate(coordinate.getFile() + 2,
                                                                  coordinate.getRank())));
            result.add(List.of(ChessCoordinate.getChessCoordinate(coordinate.getFile() - 2,
                                                                  coordinate.getRank())));
        } else {
            result.add(List.of());
            result.add(List.of());
        }

        return result;
    }

    private static List<List<ChessCoordinate>> generateRookReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.STRAIGHTS) {
            List<ChessCoordinate> ray = new ArrayList<>();
            for (ChessCoordinate currentCoord = direction.next(coordinate); currentCoord != null;
                 currentCoord = direction.next(currentCoord)) {
                ray.add(currentCoord);
            }
            result.add(ray);
        }

        return result;
    }

    private static List<List<ChessCoordinate>> generateBishopReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.DIAGONALS) {
            List<ChessCoordinate> ray = new ArrayList<>();
            for (ChessCoordinate currentCoord = direction.next(coordinate); currentCoord != null;
                 currentCoord = direction.next(currentCoord)) {
                ray.add(currentCoord);
            }
            result.add(ray);
        }

        return result;
    }

    private static List<List<ChessCoordinate>> generateKnightReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        for (Direction direction : Directions.KNIGHTS) {
            ChessCoordinate nextCoord = direction.next(coordinate);
            result.add(nextCoord == null ? List.of() : List.of(nextCoord));
        }

        return result;
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
    private static List<List<ChessCoordinate>> generateWhitePawnReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        List<ChessCoordinate> straightMoves = new ArrayList<>();
        if (coordinate.getRank() < 7) {
            // Add the Straight Moves
            straightMoves.add(UP.next(coordinate));
            if (coordinate.getRank() == 1) {
                straightMoves.add(UP.next(straightMoves.get(0)));
            }

            // Add the right diagonal
            if (coordinate.getFile() < 7) {
                result.add(List.of(UP_RIGHT.next(coordinate)));
            } else {
                result.add(List.of());
            }

            // Add the left diagonal
            if (coordinate.getFile() > 0) {
                result.add(List.of(UP_LEFT.next(coordinate)));
            } else {
                result.add(List.of());
            }
        } else {
            result.add(List.of());
            result.add(List.of());
        }
        result.add(0, straightMoves);


        return result;
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
    @SuppressWarnings("unchecked")
    private static List<List<ChessCoordinate>> generateBlackPawnReachableCoordsAt(
            ChessCoordinate coordinate) {
        List<List<ChessCoordinate>> result = new ArrayList<>();

        List<ChessCoordinate> straightMoves = new ArrayList<>();
        if (coordinate.getRank() > 0) {
            // Add the Straight Moves
            straightMoves.add(DOWN.next(coordinate));
            if (coordinate.getRank() == 6) {
                straightMoves.add(DOWN.next(straightMoves.get(0)));
            }

            // Add the right diagonal
            if (coordinate.getFile() < 7) {
                result.add(List.of(DOWN_RIGHT.next(coordinate)));
            } else {
                result.add(List.of());
            }

            // Add the left diagonal
            if (coordinate.getFile() > 0) {
                result.add(List.of(DOWN_LEFT.next(coordinate)));
            } else {
                result.add(List.of());
            }
        } else {
            result.add(List.of());
        }
        result.add(0,
                   List.of(straightMoves.toArray(straightMoves.toArray(ChessCoordinate[]::new))));

        return List.of(result.toArray(size -> (List<ChessCoordinate>[]) new List[size]));
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

    public boolean isPawn() {
        return this == WHITE_PAWN || this == BLACK_PAWN;
    }

    public boolean isBishop() {
        return this == WHITE_BISHOP || this == BLACK_BISHOP;
    }
}

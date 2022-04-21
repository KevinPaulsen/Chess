package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Piece.*;

public class BoardModel {

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final FastMap wPawns;
    private final FastMap wKnights;
    private final FastMap wBishops;
    private final FastMap wRooks;
    private final FastMap wQueens;
    private final FastMap wKing;
    private final FastMap bPawns;
    private final FastMap bKnights;
    private final FastMap bBishops;
    private final FastMap bRooks;
    private final FastMap bQueens;
    private final FastMap bKing;

    private final Map<Piece, FastMap> pieceMaps;
    private final Map<Piece, FastMap> whiteMaps;
    private final Map<Piece, FastMap> blackMaps;

    private final FastMap black;
    private final FastMap white;
    private final FastMap occupied;

    private final Zobrist zobrist;

    public BoardModel(String FEN, Zobrist zobrist) {

        wPawns = new FastMap();
        wKnights = new FastMap();
        wBishops = new FastMap();
        wRooks = new FastMap();
        wQueens = new FastMap();
        wKing = new FastMap();
        bPawns = new FastMap();
        bKnights = new FastMap();
        bBishops = new FastMap();
        bRooks = new FastMap();
        bQueens = new FastMap();
        bKing = new FastMap();

        pieceMaps = Map.ofEntries(
                Map.entry(WHITE_PAWN, wPawns),
                Map.entry(WHITE_KNIGHT, wKnights),
                Map.entry(WHITE_BISHOP, wBishops),
                Map.entry(WHITE_ROOK, wRooks),
                Map.entry(WHITE_QUEEN, wQueens),
                Map.entry(WHITE_KING, wKing),
                Map.entry(BLACK_PAWN, bPawns),
                Map.entry(BLACK_KNIGHT, bKnights),
                Map.entry(BLACK_BISHOP, bBishops),
                Map.entry(BLACK_ROOK, bRooks),
                Map.entry(BLACK_QUEEN, bQueens),
                Map.entry(BLACK_KING, bKing));
        whiteMaps = Map.ofEntries(
                Map.entry(WHITE_PAWN, wPawns),
                Map.entry(WHITE_KNIGHT, wKnights),
                Map.entry(WHITE_BISHOP, wBishops),
                Map.entry(WHITE_ROOK, wRooks),
                Map.entry(WHITE_QUEEN, wQueens),
                Map.entry(WHITE_KING, wKing));
        blackMaps = Map.ofEntries(
                Map.entry(BLACK_PAWN, bPawns),
                Map.entry(BLACK_KNIGHT, bKnights),
                Map.entry(BLACK_BISHOP, bBishops),
                Map.entry(BLACK_ROOK, bRooks),
                Map.entry(BLACK_QUEEN, bQueens),
                Map.entry(BLACK_KING, bKing));


        black = new FastMap();
        white = new FastMap();
        occupied = new FastMap();


        this.zobrist = zobrist;
        int pieceIdx = 63;
        for (char c : FEN.toCharArray()) {
            if (c == '/') {
                continue;
            }

            if (49 <= c && c <= 56) {
                pieceIdx -= c - 48;
                continue;
            }

            byte squareIdx = (byte) (pieceIdx + (7 - 2 * (pieceIdx % 8)));
            FastMap bitBoard = switch (c) {
                case 'K' -> wKing;
                case 'Q' -> wQueens;
                case 'R' -> wRooks;
                case 'B' -> wBishops;
                case 'N' -> wKnights;
                case 'P' -> wPawns;
                case 'k' -> bKing;
                case 'q' -> bQueens;
                case 'r' -> bRooks;
                case 'b' -> bBishops;
                case 'n' -> bKnights;
                case 'p' -> bPawns;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };

            bitBoard.mark(squareIdx);
            if (c < 'a') {
                white.mark(squareIdx);
            } else {
                black.mark(squareIdx);
            }
            occupied.mark(squareIdx);

            pieceIdx--;
        }
    }

    private static Map<Piece, Set<ChessCoordinate>> makePieceLocations() {
        Map<Piece, Set<ChessCoordinate>> result = new HashMap<>();
        for (Piece piece : Piece.values()) {
            if (piece == EMPTY) continue;

            result.put(piece, new HashSet<>());
        }
        return Map.copyOf(result);
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. Move is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public void move(Move move) {
        if (move != null) {
            movePiece(move.getInteractingPiece(), move.getInteractingPieceStart(), move.getInteractingPieceEnd());

            if (move.doesPromote()) {
                removePiece(move.getMovingPiece(), move.getStartingCoordinate());
                addPiece(move.getPromotedPiece(), move.getEndingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getStartingCoordinate(), move.getEndingCoordinate());
            }
        }
    }

    /**
     * Undoes the given move.
     *
     * @param move the move to undo.
     */
    public void undoMove(Move move) {
        if (move != null) {
            if (move.doesPromote()) {
                removePiece(move.getPromotedPiece(), move.getEndingCoordinate());
                addPiece(move.getMovingPiece(), move.getStartingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getEndingCoordinate(), move.getStartingCoordinate());
            }

            if (move.getInteractingPiece() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    addPiece(move.getInteractingPiece(), move.getInteractingPieceStart());
                } else {
                    movePiece(move.getInteractingPiece(), move.getInteractingPieceEnd(),
                            move.getInteractingPieceStart());
                }
            }
        }
    }

    /**
     * Gets the piece on the given ChessCoordinate.
     *
     * @param coordinate the coordinate of the requested piece is at.
     * @return the piece on the given coordinate.
     */
    public Piece getPieceOn(ChessCoordinate coordinate) {
        if (coordinate == null) return null;

        int oneDimIdx = coordinate.getOndDimIndex();

        if (!occupied.isMarked(oneDimIdx)) return null;

        if (white.isMarked(oneDimIdx)) {
            for (Map.Entry<Piece, FastMap> entry : whiteMaps.entrySet()) {
                if (entry.getValue().isMarked(oneDimIdx)) return entry.getKey();
            }
        } else {
            for (Map.Entry<Piece, FastMap> entry : blackMaps.entrySet()) {
                if (entry.getValue().isMarked(oneDimIdx)) return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Add piece to the board, and add the given moves to add to the piece.
     *
     * @param piece      the piece to add.
     * @param coordinate the coordinate to put the piece.
     */
    private void addPiece(Piece piece, ChessCoordinate coordinate) {
        if (piece != null && coordinate != null) {
            byte oneDimIdx = (byte) coordinate.getOndDimIndex();

            pieceMaps.get(piece).mark(oneDimIdx);

            occupied.mark(oneDimIdx);

            if (piece.getColor() == WHITE) white.mark(oneDimIdx);
            else black.mark(oneDimIdx);

            zobrist.addPiece(piece, coordinate);
        }
    }

    /**
     * Removes the given piece from the board.
     *
     * @param piece      the piece to remove.
     * @param coordinate the coordinate the piece is on.
     */
    private void removePiece(Piece piece, ChessCoordinate coordinate) {
        if (piece != null) {
            byte oneDimIdx = (byte) coordinate.getOndDimIndex();

            pieceMaps.get(piece).unmark(oneDimIdx);

            occupied.unmark(oneDimIdx);

            if (piece.getColor() == WHITE) white.unmark(oneDimIdx);
            else black.unmark(oneDimIdx);

            zobrist.removePiece(piece, coordinate);
        }
    }

    /**
     * Moves the piece to the given coordinate, and adds the given number of moves
     * to the piece.
     *
     * @param piece      the piece to move.
     * @param startCoord the starting coordinate of the piece.
     * @param endCoord   the ending coordinate of the piece.
     */
    public void movePiece(Piece piece, ChessCoordinate startCoord, ChessCoordinate endCoord) {
        if (piece != null) {
            removePiece(piece, startCoord);

            if (endCoord != null) {
                addPiece(piece, endCoord);
            }
        }
    }

    public List<ChessCoordinate> getLocations(Piece piece) {
        FastMap map = pieceMaps.get(piece);

        Collection<Byte> markedIndices = map.markedIndices();
        List<ChessCoordinate> locations = new ArrayList<>(8);

        for (byte coordIdx : markedIndices) {
            locations.add(ChessCoordinate.getChessCoordinate(coordIdx));
        }

        return locations;
    }

    /**
     * @return the reference to the white king.
     */
    public ChessCoordinate getWhiteKingCoord() {
        if (pieceMaps.get(WHITE_KING).getLowestSet() == 64) {
            System.out.println("oof");
        }
        return ChessCoordinate.getChessCoordinate(pieceMaps.get(WHITE_KING).getLowestSet());
    }

    /**
     * @return the reference to the black king.
     */
    public ChessCoordinate getBlackKingCoord() {
        return ChessCoordinate.getChessCoordinate(pieceMaps.get(BLACK_KING).getLowestSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel that)) return false;

        return pieceMaps.equals(that.pieceMaps);
    }

    @Override
    public int hashCode() {
        return (int) zobrist.getHashValue();
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[] getPieceArray() {
        Piece[] pieceArray = new Piece[64];

        for (int idx = 0; idx < pieceArray.length; idx++) {
            pieceArray[idx] = getPieceOn(ChessCoordinate.getChessCoordinate(idx));
        }

        return pieceArray;
    }

    public boolean isOccupiedByColor(ChessCoordinate potentialCoordinate, char color) {
        if (color == WHITE) {
            return white.isMarked(potentialCoordinate.getOndDimIndex());
        } else {
            return black.isMarked(potentialCoordinate.getOndDimIndex());
        }
    }

    public boolean isKing(ChessCoordinate coordinate) {
        return wKing.isMarked(coordinate.getOndDimIndex()) || bKing.isMarked(coordinate.getOndDimIndex());
    }

    public boolean isQueen(ChessCoordinate coordinate) {
        return wQueens.isMarked(coordinate.getOndDimIndex()) || bQueens.isMarked(coordinate.getOndDimIndex());
    }

    public boolean isBishop(ChessCoordinate coordinate) {
        return wBishops.isMarked(coordinate.getOndDimIndex()) || bBishops.isMarked(coordinate.getOndDimIndex());
    }

    public boolean isRook(ChessCoordinate coordinate) {
        return wRooks.isMarked(coordinate.getOndDimIndex()) || bRooks.isMarked(coordinate.getOndDimIndex());
    }

    public boolean isOccupied(ChessCoordinate potentialCoordinate) {
        return occupied.isMarked(potentialCoordinate.getOndDimIndex());
    }
}

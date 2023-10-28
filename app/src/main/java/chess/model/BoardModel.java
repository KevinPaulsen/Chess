package chess.model;

import chess.ChessCoordinate;
import chess.model.moves.Movable;
import chess.model.pieces.Piece;
import chess.util.BitIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static chess.model.GameModel.WHITE;
import static chess.model.pieces.Piece.*;

public class BoardModel {

    private final ArrayDeque<BoardState> stateHistory;

    private long hashValue = 0x0L;

    public BoardModel(String FEN) {
        stateHistory = new ArrayDeque<>();

        long[] pieceMaps = new long[values().length];
        long white = 0x0L;
        long black = 0x0L;

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
            Piece piece = switch (c) {
                case 'K' -> WHITE_KING;
                case 'Q' -> WHITE_QUEEN;
                case 'R' -> WHITE_ROOK;
                case 'B' -> WHITE_BISHOP;
                case 'N' -> WHITE_KNIGHT;
                case 'P' -> WHITE_PAWN;
                case 'k' -> BLACK_KING;
                case 'q' -> BLACK_QUEEN;
                case 'r' -> BLACK_ROOK;
                case 'b' -> BLACK_BISHOP;
                case 'n' -> BLACK_KNIGHT;
                case 'p' -> BLACK_PAWN;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };

            long squareMask = ChessCoordinate.getChessCoordinate(squareIdx).getBitMask();

            pieceMaps[piece.ordinal()] |= squareMask;

            if (c < 'a') {
                white |= squareMask;
            } else {
                black |= squareMask;
            }

            hashValue = Zobrist.flipPiece(piece, ChessCoordinate.getChessCoordinate(squareIdx),
                    hashValue);

            pieceIdx--;
        }
        stateHistory.add(new BoardState(pieceMaps, white, black, white | black, hashValue));
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. NormalMove is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public long move(Movable move) {
        if (move != null) {
            BoardState nextState = move.nextState(getState());
            stateHistory.push(nextState);
            hashValue ^= nextState.deltaHash;
        }
        return hashValue;
    }

    /**
     * Undoes the given move.
     */
    public long undoMove() {
        long deltaHash = stateHistory.pop().deltaHash;
        hashValue ^= deltaHash;
        return hashValue;
    }

    private BoardState getState() {
        return stateHistory.peek();
    }

    /**
     * Gets the piece on the given ChessCoordinate.
     *
     * @param coordinate the coordinate of the requested piece is at.
     * @return the piece on the given coordinate.
     */
    public Piece getPieceOn(ChessCoordinate coordinate) {
        return getState().getPieceOn(coordinate.getBitMask());
    }

    public List<ChessCoordinate> getLocations(Piece piece) {
        BitIterator iterator = new BitIterator(getState().pieceMaps[piece.ordinal()]);
        List<ChessCoordinate> locations = new ArrayList<>(8);
        iterator.forEachRemaining(locations::add);

        return locations;
    }

    /**
     * @return the reference to the white king.
     */
    public ChessCoordinate getWhiteKingCoord() {
        return ChessCoordinate.getChessCoordinate(
                Long.numberOfTrailingZeros(getState().pieceMaps[WHITE_KING.ordinal()]));
    }

    /**
     * @return the reference to the black king.
     */
    public ChessCoordinate getBlackKingCoord() {
        return ChessCoordinate.getChessCoordinate(
                Long.numberOfTrailingZeros(getState().pieceMaps[BLACK_KING.ordinal()]));
    }

    @Override
    public int hashCode() {
        return (int) hashValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BoardModel that))
            return false;

        return getState().equals(that.getState());
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

    public boolean coordIsPiece(Piece piece, ChessCoordinate coordinate) {
        return (getState().pieceMaps[piece.ordinal()] & coordinate.getBitMask()) != 0;
    }

    public boolean isPawn(ChessCoordinate coordinate) {
        return coordIsPiece(WHITE_PAWN, coordinate) || coordIsPiece(BLACK_PAWN, coordinate);
    }

    public long getOccupancyMap() {
        return getState().occupied;
    }

    public long getOccupancyMap(char color) {
        return color == WHITE ? getState().white : getState().black;
    }

    public long getPieceMap(Piece piece) {
        return getState().pieceMaps[piece.ordinal()];
    }

    public long getHashValue() {
        return hashValue;
    }

    public record BoardState(long[] pieceMaps, long white, long black, long occupied,
                             long deltaHash) {

        public Piece getPieceOn(long coordinateMask) {
            if ((occupied & coordinateMask) != 0) {
                if ((white & coordinateMask) != 0) {
                    if ((pieceMaps[WHITE_PAWN.ordinal()] & coordinateMask) != 0)
                        return WHITE_PAWN;
                    if ((pieceMaps[WHITE_KNIGHT.ordinal()] & coordinateMask) != 0)
                        return WHITE_KNIGHT;
                    if ((pieceMaps[WHITE_BISHOP.ordinal()] & coordinateMask) != 0)
                        return WHITE_BISHOP;
                    if ((pieceMaps[WHITE_ROOK.ordinal()] & coordinateMask) != 0)
                        return WHITE_ROOK;
                    if ((pieceMaps[WHITE_QUEEN.ordinal()] & coordinateMask) != 0)
                        return WHITE_QUEEN;
                    if ((pieceMaps[WHITE_KING.ordinal()] & coordinateMask) != 0)
                        return WHITE_KING;
                } else {
                    if ((pieceMaps[BLACK_PAWN.ordinal()] & coordinateMask) != 0)
                        return BLACK_PAWN;
                    if ((pieceMaps[BLACK_KNIGHT.ordinal()] & coordinateMask) != 0)
                        return BLACK_KNIGHT;
                    if ((pieceMaps[BLACK_BISHOP.ordinal()] & coordinateMask) != 0)
                        return BLACK_BISHOP;
                    if ((pieceMaps[BLACK_ROOK.ordinal()] & coordinateMask) != 0)
                        return BLACK_ROOK;
                    if ((pieceMaps[BLACK_QUEEN.ordinal()] & coordinateMask) != 0)
                        return BLACK_QUEEN;
                    if ((pieceMaps[BLACK_KING.ordinal()] & coordinateMask) != 0)
                        return BLACK_KING;
                }
            }
            return null;
        }
    }
}

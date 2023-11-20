package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.Random;

import static chess.ChessCoordinate.A1;
import static chess.model.GameModel.WHITE;

public abstract class Zobrist {

    private static final Random RANDOM = new Random(0);

    private static final long[][] zobristHashTable = makeHashTable();
    private static final long[] enPassantCoordTable = makeTable(17);
    private static final long[] castlingHashTable = makeTable(16);
    private static final long[] numTimesReachedTable = makeTable(3);
    private static final long[] sideToMove = makeTable(2);

    private static long[][] makeHashTable() {
        long[][] result = new long[64][13];

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            for (int pieceIdx = 0; pieceIdx < 13; pieceIdx++) {
                result[coordIdx][pieceIdx] = generateHash();
            }
        }

        return result;
    }

    private static long generateHash() {
        long hash = 0;
        for (int bit = 0; bit < 64; bit++) {
            if (RANDOM.nextBoolean()) {
                hash |= 1L << bit;
            }
        }
        return hash;
    }

    private static long[] makeTable(int size) {
        long[] result = new long[size];

        for (int coordIdx = 0; coordIdx < size; coordIdx++) {
            result[coordIdx] = generateHash();
        }

        return result;
    }

    public static long slowZobrist(GameModel game) {
        long hashValue = 0x0L;
        BoardModel board = game.getBoard();

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            Piece piece = board.getPieceOn(ChessCoordinate.getChessCoordinate(coordIdx));
            if (piece != null) {
                hashValue ^= zobristHashTable[coordIdx][piece.getUniqueIdx()];
            }
        }

        long stateHashValue = 0x0L;

        ChessCoordinate enPassant = ChessCoordinate.getChessCoordinate(game.getEnPassantTarget());
        int addedEnPassantTarget =
                enPassant == null ? 16 : enPassant.getFile() + (enPassant.getRank() == 2 ? 0 : 8);
        stateHashValue ^= enPassantCoordTable[addedEnPassantTarget];
        int addedCastlingData = (int) (game.getGameState().getMap() & 0b1111L);
        stateHashValue ^= castlingHashTable[addedCastlingData];
        int addedSideToMove = game.getTurn() == WHITE ? 0 : 1;
        stateHashValue ^= sideToMove[addedSideToMove];

        return hashValue ^ stateHashValue;
    }

    public static long flipPiece(Piece piece, int squareIndex, long hashValue) {
        return hashValue ^ zobristHashTable[squareIndex][piece.getUniqueIdx()];
    }

    public static long getGameStateHash(FastMap gameState) {
        long hashValue = 0x0L;

        int currentCastlingData = (int) (gameState.getMap() & 0b1111L);
        hashValue ^= castlingHashTable[currentCastlingData];

        ChessCoordinate enPassant = ChessCoordinate.getChessCoordinate(
                (int) gameState.getMap() >> 7);
        enPassant = enPassant == A1 ? null : enPassant;
        int addedEnPassantTarget =
                enPassant == null ? 16 : enPassant.getFile() + (enPassant.getRank() == 2 ? 0 : 8);
        hashValue ^= enPassantCoordTable[addedEnPassantTarget];

        int currentSideToMove = gameState.isMarked(4) ? 0 : 1;
        hashValue ^= sideToMove[currentSideToMove];

        return hashValue;
    }

    public static long getHashValueWithTimesMoved(long hashValue, int numTimesReached) {
        if (3 < numTimesReached || numTimesReached < 0) {
            throw new IllegalStateException(
                    "Times moved must be between 0 and 3 (inclusive). " + "Passed in: " +
                            numTimesReached);
        }

        return hashValue ^ numTimesReachedTable[numTimesReached - 1];
    }
}

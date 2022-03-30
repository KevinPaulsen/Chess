package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.Random;

import static chess.model.GameModel.WHITE;

public class Zobrist {

    private static final Random RANDOM = new Random(0);

    private static final long[][] zobristHashTable = makeHashTable();
    private static final long[] enPassantCoordTable = makeTable(9);
    private static final long[] castlingHashTable = makeTable(16);
    private static final long[] numTimesReachedTable = makeTable(3);
    private static final long[] sideToMove = makeTable(2);

    private int addedEnPassantTarget;
    private int addedCastlingData;
    private int addedSideToMove;
    private long hashValue;

    public Zobrist(Zobrist zobrist) {
        this(zobrist.addedEnPassantTarget, zobrist.addedCastlingData, zobrist.addedSideToMove, zobrist.hashValue);
    }

    public Zobrist() {
        this(0, 0, 0, 0);
    }

    public Zobrist(int addedEnPassantTarget, int addedCastlingData, int addedSideToMove, long hashValue) {
        this.addedEnPassantTarget = addedEnPassantTarget;
        this.addedCastlingData = addedCastlingData;
        this.addedSideToMove = addedSideToMove;
        this.hashValue = hashValue;
    }

    private static long[][] makeHashTable() {
        long[][] result = new long[64][13];

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            for (int pieceIdx = 0; pieceIdx < 13; pieceIdx++) {
                result[coordIdx][pieceIdx] = generateHash();
            }
        }

        return result;
    }

    private static long[] makeTable(int size) {
        long[] result = new long[size];

        for (int coordIdx = 0; coordIdx < size; coordIdx++) {
            result[coordIdx] = generateHash();
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

    public void slowZobrist(GameModel game) {
        BoardModel board = game.getBoard();

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            Piece piece = board.getPieceOn(ChessCoordinate.getChessCoordinate(coordIdx));
            if (piece != null) {
                hashValue ^= zobristHashTable[coordIdx][piece.getUniqueIdx()];
            }
        }

        ChessCoordinate enPassant = game.getEnPassantTarget();
        addedEnPassantTarget = enPassant == null ? 0 : enPassant.getFile();
        hashValue ^= enPassantCoordTable[addedEnPassantTarget];
        addedCastlingData = (int) (game.getGameState().getMap() & 0b1111L);
        hashValue ^= castlingHashTable[addedCastlingData];
        addedSideToMove = game.getTurn() == WHITE ? 0 : 1;
        hashValue ^= sideToMove[addedSideToMove];
    }

    public void addPiece(Piece piece, ChessCoordinate coordinate) {
        hashValue ^= zobristHashTable[coordinate.getOndDimIndex()][piece.getUniqueIdx()];
    }

    public void removePiece(Piece piece, ChessCoordinate coordinate) {
        hashValue ^= zobristHashTable[coordinate.getOndDimIndex()][piece.getUniqueIdx()];
    }

    public void updateGameData(FastMap newState) {
        int currentCastlingData = (int) (newState.getMap() & 0b1111L);

        if (addedCastlingData != currentCastlingData) {
            hashValue ^= castlingHashTable[addedCastlingData];
            addedCastlingData = currentCastlingData;
            hashValue ^= castlingHashTable[addedCastlingData];
        }

        int currentEnPassantTarget = (int) newState.getMap() >> 7;
        currentEnPassantTarget = currentEnPassantTarget == 0 ? 0 : 1 + currentEnPassantTarget % 8;

        if (addedEnPassantTarget != currentEnPassantTarget) {
            hashValue ^= enPassantCoordTable[addedEnPassantTarget];
            addedEnPassantTarget = currentEnPassantTarget;
            hashValue ^= enPassantCoordTable[addedEnPassantTarget];
        }

        int currentSideToMove = newState.isMarked(4) ? 0 : 1;

        if (addedSideToMove != currentSideToMove) {
            hashValue ^= sideToMove[addedSideToMove];
            addedSideToMove = currentSideToMove;
            hashValue ^= sideToMove[addedSideToMove];
        }
    }

    public long getHashValue() {
        return hashValue;
    }

    public long getHashValueWithTimesMoved(int numTimesReached) {
        if (3 < numTimesReached || numTimesReached < 0) {
            throw new IllegalStateException("Times moved must be between 0 and 3 (inclusive). " +
                    "Passed in: " + numTimesReached);
        }

        return hashValue ^ numTimesReachedTable[numTimesReached - 1];
    }
}

package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.Arrays;
import java.util.Random;

public class Zobrist {

    private final long[][] zobristHashTable;
    private final long[] enPassantCoordTable;
    private final long[] castlingHashTable;
    private final long sideToMove;

    private int addedEnPassantTarget = 0;
    private int addedCastlingData = 0;
    private long hashValue = 0;

    public Zobrist(Zobrist zobrist) {
        zobristHashTable = zobrist.zobristHashTable;
        enPassantCoordTable = zobrist.enPassantCoordTable;
        castlingHashTable = zobrist.castlingHashTable;
        sideToMove = zobrist.sideToMove;

        addedEnPassantTarget = zobrist.addedEnPassantTarget;
        addedCastlingData = zobrist.addedCastlingData;
        hashValue = zobrist.hashValue;
    }

    public Zobrist() {
        Random random = new Random(0);
        this.zobristHashTable = makeHashTable(random);
        this.enPassantCoordTable = enPassantCoordTable(random);
        this.castlingHashTable = makeCastlingTable(random);
        this.sideToMove = generateHash(random);
    }

    public void slowZobrist(GameModel game) {
        BoardModel board = game.getBoard();

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            Piece piece = board.getPieceOn(BoardModel.getChessCoordinate(coordIdx));
            if (piece != null) {
                hashValue ^= zobristHashTable[coordIdx][piece.getUniqueIdx()];
            }
        }

        ChessCoordinate enPassant = game.getEnPassantTarget();
        addedEnPassantTarget = enPassant == null ? 0 : enPassant.getFile();
        hashValue ^= enPassantCoordTable[addedEnPassantTarget];
        addedCastlingData = (int) (game.getGameState().getMap() & 0b1111L);
        hashValue ^= castlingHashTable[addedCastlingData];
        hashValue ^= game.getTurn() == GameModel.WHITE ? sideToMove : 0;
    }

    public void addPiece(Piece piece, ChessCoordinate coordinate) {
        hashValue ^= zobristHashTable[coordinate.getOndDimIndex()][piece.getUniqueIdx()];
    }

    public void removePiece(Piece piece, ChessCoordinate coordinate) {
        hashValue ^= zobristHashTable[coordinate.getOndDimIndex()][piece.getUniqueIdx()];
    }

    private static long[][] makeHashTable(Random random) {
        long[][] result = new long[64][13];

        for (int coordIdx = 0; coordIdx < 64; coordIdx++) {
            for (int pieceIdx = 0; pieceIdx < 13; pieceIdx++) {
                result[coordIdx][pieceIdx] = generateHash(random);
            }
        }

        return result;
    }

    private static long[] enPassantCoordTable(Random random) {
        long[] result = new long[9];

        for (int coordIdx = 0; coordIdx < 9; coordIdx++) {
            result[coordIdx] = generateHash(random);
        }

        return result;
    }

    private static long[] makeCastlingTable(Random random) {
        long[] result = new long[16];

        for (int coordIdx = 0; coordIdx < 16; coordIdx++) {
            result[coordIdx] = generateHash(random);
        }

        return result;
    }

    private static long generateHash(Random random) {
        long hash = 0;
        for (int bit = 0; bit < 64; bit++) {
            if (random.nextBoolean()) {
                hash |= 1L << bit;
            }
        }
        return hash;
    }

    public long getHashValue() {
        return hashValue;
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

        hashValue ^= sideToMove;
    }
}

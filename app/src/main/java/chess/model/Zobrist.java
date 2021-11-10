package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Piece;
import chess.util.FastMap;

import java.util.Objects;
import java.util.Random;

public class Zobrist {

    private final long[][] zobristHashTable;
    private final long[] enPassantCoordTable;
    private final long[] castlingHashTable;
    private final long sideToMove;

    private long hashValue = 0;

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
        hashValue ^= enPassantCoordTable[enPassant == null ? 0 : enPassant.getFile()];
        hashValue ^= castlingHashTable[(int) (game.getGameState().getMap() & 0b1111L)];
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

    public void updateGameData(GameModel game, FastMap newState) {
        int previousCastlingData = (int) (game.getGameState().getMap() & 0b1111L);
        int currentCastlingData = (int) (newState.getMap() & 0b1111L);
        if (previousCastlingData != currentCastlingData) {
            hashValue ^= castlingHashTable[previousCastlingData];
            hashValue ^= castlingHashTable[currentCastlingData];
        }

        if (game.getEnPassantTarget() != null) {
            hashValue ^= enPassantCoordTable[game.getEnPassantTarget().getFile()];
        }
        int currentEnPassantTarget = (int) newState.getMap() >> 7;
        if (currentEnPassantTarget != 0) {
            hashValue ^= enPassantCoordTable[BoardModel.getChessCoordinate(currentEnPassantTarget).getFile()];
        }

        hashValue ^= sideToMove;
    }
}

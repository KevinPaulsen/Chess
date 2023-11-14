package chess.model.chessai;

import chess.model.GameModel;
import chess.model.moves.Movable;

public class TranspositionTable {

    public static final int UNKNOWN = Integer.MIN_VALUE;
    public static final byte EXACT = 0;
    public static final byte ALPHA = 1;
    public static final byte BETA = 2;

    private final TableEntry[] transpositionTable;
    private final int size;

    public TranspositionTable(int size) {
        this.transpositionTable = new TableEntry[size];
        this.size = size;
    }

    public int probeHash(long hash, int depth, int alpha, int beta) {
        TableEntry transposition = getHash(hash);

        if (transposition != null && transposition.hash == hash) {
            if (transposition.depth >= depth) {
                if (transposition.flag == EXACT) {
                    return transposition.evaluation;
                }

                if (transposition.flag == ALPHA && transposition.evaluation <= alpha) {
                    return alpha;
                }

                if (transposition.flag == BETA && transposition.evaluation >= beta) {
                    return beta;
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    private TableEntry getHash(long hash) {
        return transpositionTable[Math.abs((int) (hash % size))];
    }

    public void recordHash(long hash, Movable move, int depth, int evaluation, byte flag) {
        recordHash(new TableEntry(hash, move, evaluation, flag, depth));
    }

    private void recordHash(TableEntry transposition) {
        int index = Math.abs((int) (transposition.hash % size));

        TableEntry currentValue = transpositionTable[index];

        if (currentValue == null || transposition.depth >= currentValue.depth) {
            transpositionTable[index] = transposition;
        }
    }

    public Movable bestMove(long hash) {
        TableEntry transposition = getHash(hash);

        if (transposition != null && transposition.hash == hash) {
            return transposition.bestMove;
        }

        return null;
    }

    public void printEvaluation(long zobristHash, GameModel game) {
        TableEntry current = getHash(zobristHash);

        System.out.printf("%2d ply = %-5d | %s", current.depth, current.evaluation,
                          current.bestMove);
        game.move(current.bestMove);
        current = getHash(game.getZobristHash());

        while (current != null && current.bestMove != null &&
                game.getLegalMoves().toList().contains(current.bestMove)) {
            System.out.printf(" -> %s", current.bestMove);
            game.move(current.bestMove);
            current = getHash(game.getZobristHash());
        }

        System.out.println();
    }

    private record TableEntry(long hash, Movable bestMove, int evaluation, byte flag, int depth) {}
}

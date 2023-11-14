package chess.model.chessai;

import chess.model.GameModel;
import chess.model.moves.Movable;

import static chess.model.GameModel.WHITE;

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

    public Evaluation probeHash(long hash, int depth, int alpha, int beta) {
        TableEntry transposition = getHash(hash);

        if (transposition != null && transposition.hash == hash) {
            if (transposition.depth >= depth) {
                if (transposition.flag == EXACT) {
                    return transposition.evaluation;
                }

                if (transposition.flag == ALPHA && transposition.evaluation.getScore() <= alpha) {
                    return new Evaluation(alpha, 0);
                }

                if (transposition.flag == BETA && transposition.evaluation.getScore() >= beta) {
                    return new Evaluation(beta, 0);
                }
            }
        }

        return null;
    }

    private TableEntry getHash(long hash) {
        return transpositionTable[Math.abs((int) (hash % size))];
    }

    public void recordHash(long hash, Evaluation evaluation, byte flag) {
        recordHash(new TableEntry(hash, evaluation, flag, evaluation.getDepth()));
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
            return transposition.evaluation().getMove();
        }

        return null;
    }

    public void printEvaluation(GameModel game, Movable bestMove, int depth, int evaluation) {
        evaluation *= bestMove.getMovingPiece().getColor() == WHITE ? 1 : -1;
        System.out.printf("%2d ply = %-5d | %-5s", depth--, evaluation,
                          game.getMoveString(bestMove));

        game.move(bestMove);
        TableEntry current = getHash(game.getZobristWithTimesMoved());

        while (current != null && game.getGameOverStatus() == GameModel.IN_PROGRESS &&
                depth-- > 0) {

            if (current.evaluation().getMove() == null) {
                System.out.print(" -> null ");
                break;
            } else if (!game.getLegalMoves().toList().contains(current.evaluation().getMove())) {
                break;
            }

            System.out.printf(" -> %-5s", game.getMoveString(current.evaluation().getMove()));
            game.move(current.evaluation().getMove());
            current = getHash(game.getZobristWithTimesMoved());
        }

        System.out.println();
    }

    private record TableEntry(long hash, Evaluation evaluation, byte flag, int depth) {}
}

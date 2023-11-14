package chess.model.chessai;


import chess.model.GameModel;
import chess.model.moves.Movable;

import java.util.List;

import static chess.model.GameModel.IN_PROGRESS;
import static chess.model.chessai.TranspositionTable.*;

public class Searcher {

    private static final int WORST_EVAL = Integer.MIN_VALUE + 1;
    private static final int BEST_EVAL = Integer.MAX_VALUE;

    private final TranspositionTable transpositionTable;
    private final Evaluator evaluator;
    private volatile boolean canceled;

    public Searcher(Evaluator evaluator, TranspositionTable transpositionTable) {
        this.evaluator = evaluator;
        this.canceled = false;
        this.transpositionTable = transpositionTable;
    }

    public Evaluation getBestMove(GameModel game, int depth) {
        int alpha = WORST_EVAL;
        Movable bestMove = null;

        List<Movable> sortedMoves = evaluator.getSortedMoves(game, null);
        for (Movable move : sortedMoves) {
            game.move(move);
            int eval = -search(game, depth - 1, WORST_EVAL, -alpha);
            game.undoLastMove();

            if (eval == WORST_EVAL) {
                break;
            }

            if (eval > alpha) {
                alpha = eval;
                bestMove = move;
            }
        }

        if (canceled) {
            return null;
        }

        transpositionTable.recordHash(game.getZobristWithTimesMoved(), bestMove, depth, alpha,
                                      EXACT);
        return new Evaluation(bestMove, alpha);
    }

    public void cancel() {
        canceled = true;
    }

    private int search(GameModel game, int depth, int alpha, int beta) {
        if (canceled) {
            return WORST_EVAL;
        }

        long hashValue = game.getZobristWithTimesMoved();
        int evaluation = transpositionTable.probeHash(hashValue, depth, alpha, beta);

        if (evaluation != UNKNOWN) {
            return evaluation;
        } else if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            evaluation = evaluator.evaluate(game);
            transpositionTable.recordHash(hashValue, null, 0, evaluation, EXACT);
            return evaluation;
        }

        byte hashFlag = ALPHA;
        Movable bestMove = null;
        List<Movable> sortedMoves = evaluator.getSortedMoves(game, transpositionTable.bestMove(
                hashValue));

        for (Movable move : sortedMoves) {
            game.move(move);
            int eval = -search(game, depth - 1, -beta, -alpha);
            game.undoLastMove();

            // Means a cancellation happened
            if (canceled) {
                return WORST_EVAL;
            }

            if (eval >= beta) {
                transpositionTable.recordHash(hashValue, move, depth, beta, BETA);
                return beta;
            } else if (eval > alpha) {
                hashFlag = EXACT;
                alpha = eval;
                bestMove = move;
            }
        }

        transpositionTable.recordHash(hashValue, bestMove, depth, alpha, hashFlag);
        return alpha;
    }

    public record Evaluation(Movable move, int score) {}
}

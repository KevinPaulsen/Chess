package chess.model.chessai;


import chess.model.GameModel;
import chess.model.moves.Movable;

import java.util.List;

import static chess.model.GameModel.IN_PROGRESS;
import static chess.model.chessai.Evaluation.WORST_EVALUATION;
import static chess.model.chessai.TranspositionTable.*;

public class Searcher {

    private static final int WORST_SCORE = Integer.MIN_VALUE + 1;
    private static final int BEST_SCORE = Integer.MAX_VALUE;

    private final TranspositionTable transpositionTable;
    private final Evaluator evaluator;
    private volatile boolean canceled;

    public Searcher(Evaluator evaluator, TranspositionTable transpositionTable) {
        this.evaluator = evaluator;
        this.canceled = false;
        this.transpositionTable = transpositionTable;
    }

    public Evaluation getBestMove(GameModel game, int depth) {
        return search(game, depth, WORST_SCORE, BEST_SCORE);
    }

    public void cancel() {
        canceled = true;
    }

    private Evaluation search(GameModel game, int depth, int alpha, int beta) {
        if (canceled) {
            return WORST_EVALUATION;
        }

        long hashValue = game.getZobristWithTimesMoved();
        int evaluation = transpositionTable.probeHash(hashValue, depth, alpha, beta);

        if (evaluation != UNKNOWN) {
            return new Evaluation(transpositionTable.bestMove(hashValue), null, evaluation, depth);
        } else if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            evaluation = evaluator.evaluate(game);
            transpositionTable.recordHash(hashValue, null, 0, evaluation, EXACT);
            return new Evaluation(null, null, evaluation, 0);
        }

        byte hashFlag = ALPHA;
        Movable bestMove = null;
        List<Movable> sortedMoves = evaluator.getSortedMoves(game, transpositionTable.bestMove(
                hashValue));

        for (Movable move : sortedMoves) {
            game.move(move);
            Evaluation eval = search(game, depth - 1, -beta, -alpha);
            game.undoLastMove();

            // Means a cancellation happened
            if (canceled) {
                return WORST_EVALUATION;
            }

            int score = -eval.getScore();
            if (score >= beta) {
                transpositionTable.recordHash(hashValue, move, depth, beta, BETA);
                return new Evaluation(move, null, beta, depth);
            } else if (score > alpha) {
                hashFlag = EXACT;
                alpha = score;
                bestMove = move;
            }
        }

        transpositionTable.recordHash(hashValue, bestMove, depth, alpha, hashFlag);
        return new Evaluation(bestMove, null, alpha, depth);
    }
}

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
        Evaluation evaluation = transpositionTable.probeHash(hashValue, depth, alpha, beta);

        if (evaluation != null) {
            return new Evaluation(transpositionTable.bestMove(hashValue), evaluation.getChild(),
                                  evaluation.getScore(), depth);
        } else if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            evaluation = evaluator.evaluate(game);
            transpositionTable.recordHash(hashValue, evaluation, EXACT);
            return new Evaluation(null, evaluation, evaluation.getScore(), 0);
        }

        byte hashFlag = ALPHA;
        Movable bestMove = null;
        Evaluation bestEval = null;
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
                Evaluation result = new Evaluation(move, eval, beta, depth);
                transpositionTable.recordHash(hashValue, result, BETA);
                return result;
            } else if (score > alpha) {
                hashFlag = EXACT;
                alpha = score;
                bestMove = move;
                bestEval = eval;
            }
        }

        Evaluation result = new Evaluation(bestMove, bestEval, alpha, depth);
        transpositionTable.recordHash(hashValue, result, hashFlag);
        return result;
    }
}

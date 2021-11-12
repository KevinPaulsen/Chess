package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chess.model.GameModel.IN_PROGRESS;

/**
 * This class represents the computer player in a chess algorithm.
 * This class takes an evaluator, and uses the mini-max algorithm to
 * find the best move.
 */
public class ChessAI {

    /**
     * The depth to search to
     */
    private static final int MIN_DEPTH = 1;
    private static final long TIME_CUTOFF = 500;

    /**
     * This table stores a position hash, and maps it to the found
     * evaluation. This may result in speed up due to transpositions
     * to the same position.
     */
    private final Map<Long, Evaluation> transpositionTable;

    /**
     * The evaluator this class uses to evaluate positions, and moves.
     */
    private final Evaluator evaluator;

    /**
     * The game this AI is playing in.
     */
    private final GameModel game;

    private long startTime = 0;

    /**
     * Simple constructor that makes a new ChessAI with the given
     * evaluator and game.
     *
     * @param evaluator the evaluator this AI uses.
     * @param game the game this AI is in.
     */
    public ChessAI(Evaluator evaluator, GameModel game) {
        this.evaluator = evaluator;
        this.game = game;
        this.transpositionTable = new HashMap<>();
    }

    /**
     * Search and find the best move in the current position.
     *
     * @return the best move to DEPTH, according to the evaluator.
     */
    public Move getBestMove() {
        boolean maximizingPlayer = game.getTurn() == GameModel.WHITE;

        long hash = game.getZobristHash();
        startTime = System.currentTimeMillis();
        long endTime = startTime;
        int depth = 1;

        Evaluation bestEvalToLatestDepth = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        do {
            Evaluation hashEval = transpositionTable.get(hash);
            if (hashEval != null && hashEval.getDepth() >= depth) {
                bestEvalToLatestDepth = hashEval;
                depth++;
                endTime = System.currentTimeMillis();

                if (hashEval.getLoser() == Evaluation.NO_LOSER) {
                    continue;
                } else {
                    break;
                }
            } // */

            AlphaBeta alphaBeta = new AlphaBeta();
            Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
            for (Move move : evaluator.getSortedMoves(game, hashEval == null ? null : hashEval.getMove())) {
                bestEval = getEvaluation(maximizingPlayer, bestEval, alphaBeta, move, depth);
                endTime = System.currentTimeMillis();
                if (endTime - startTime > TIME_CUTOFF) break;
            }
            if (endTime - startTime > TIME_CUTOFF) break;

            if (bestEval == Evaluation.BEST_EVALUATION || bestEval == Evaluation.WORST_EVALUATION) {
                break;
            }
            transpositionTable.put(hash, bestEval);
            depth++;
            endTime = System.currentTimeMillis();
            bestEvalToLatestDepth = bestEval;
        } while (endTime - startTime < TIME_CUTOFF || depth <= MIN_DEPTH);

        System.out.println(bestEvalToLatestDepth);
        return bestEvalToLatestDepth.getMove();
    }

    /**
     * Get the evaluation of the current position to the given depth.
     * This is an implementation of the minimax algorithm to simulate
     * optimal play by both players.
     *
     * @param alphaBeta the AlphaBeta object used for pruning.
     * @param depth the depth to search to, each depth is 1 ply.
     * @return the Evaluation of the current position assuming optimal play.
     */
    private Evaluation miniMax(AlphaBeta alphaBeta, int depth) {

        long hash = game.getZobristHash();
        Evaluation hashEval = transpositionTable.get(hash);
        if (hashEval != null && hashEval.getDepth() >= depth) {
            return hashEval;
        }

        if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            return evaluator.evaluate(game);
        }
        boolean maximizingPlayer = game.getTurn() == 'w';

        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;
        List<Move> sortedMoves = evaluator.getSortedMoves(game, hashEval == null ? null : hashEval.getMove());
        for (Move move : sortedMoves) {
            bestEval = getEvaluation(maximizingPlayer, bestEval, alphaBeta, move, depth);
            if (!sortedMoves.contains(bestEval.getMove())) {
                throw new IllegalStateException("what is wrong?");
            }

            if (alphaBeta.betaLessThanAlpha()) {
                break;
            }
        }

        if (bestEval != Evaluation.BEST_EVALUATION && bestEval != Evaluation.WORST_EVALUATION) {
            transpositionTable.put(hash, bestEval);
        }

        return bestEval;
    }

    /**
     * Evaluates a given move to the given depth. Calls the minimax
     * algorithm to evaluate the move.
     *
     * @param maximizingPlayer if the moving player is maximizing.
     * @param bestEval the best move found so far.
     * @param alphaBeta the AlphaBeta object, used for pruning.
     * @param move the move to evaluate.
     * @param depth the depth to search to.
     * @return the Evaluation of the given move.
     */
    private Evaluation getEvaluation(boolean maximizingPlayer, Evaluation bestEval, AlphaBeta alphaBeta,
                                     Move move, int depth) {
        // Make the given move.
        game.move(move);

        // Evaluate the position
        Evaluation moveEval = miniMax(new AlphaBeta(alphaBeta), depth - 1);

        // Undo the move
        game.undoMove(move);

        // Update the best move so far, and update the AlphaBeta objects.
        if (maximizingPlayer) {
            bestEval = Evaluation.max(bestEval, new Evaluation(move, moveEval));
            alphaBeta.alphaMax(bestEval.getEvaluation());
        } else {
            bestEval = Evaluation.min(bestEval, new Evaluation(move, moveEval));
            alphaBeta.betaMin(bestEval.getEvaluation());
        }
        return bestEval;
    }

    private static class AlphaBeta {
        double alpha;
        double beta;

        private AlphaBeta() {
            this(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        private AlphaBeta(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        private AlphaBeta(AlphaBeta alphaBeta) {
            alpha = alphaBeta.alpha;
            beta = alphaBeta.beta;
        }

        private boolean betaLessThanAlpha() {
            return beta <= alpha;
        }

        private void alphaMax(double eval) {
            if (alpha < eval) {
                alpha = eval;
            }
        }

        private void betaMin(double eval) {
            if (eval < beta) {
                beta = eval;
            }
        }
    }
}

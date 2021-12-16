package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static chess.model.GameModel.IN_PROGRESS;
import static chess.model.chessai.TranspositionTable.*;

/**
 * This class represents the computer player in a chess algorithm.
 * This class takes an evaluator, and uses the mini-max algorithm to
 * find the best move.
 */
public class ChessAI {

    /**
     * This table stores a position hash, and maps it to the found
     * evaluation. This may result in speed up due to transpositions
     * to the same position.
     */
    private final TranspositionTable transpositionTable;

    /**
     * The evaluator this class uses to evaluate positions, and moves.
     */
    private final Evaluator evaluator;

    /**
     * The game this AI is playing in.
     */
    private final GameModel game;

    /**
     * Simple constructor that makes a new ChessAI with the given
     * evaluator and game.
     *
     * @param evaluator the evaluator this AI uses.
     * @param game      the game this AI is in.
     */
    public ChessAI(Evaluator evaluator, GameModel game) {
        this.evaluator = evaluator;
        this.game = game;
        this.transpositionTable = new TranspositionTable();
    }

    /**
     * Returns the best move to the given depth. This does not use iterative deepening.
     *
     * @param depth the depth to search to
     * @return the best move in the position
     */
    public Move getBestMove(int depth) {
        return getBestMove(false, depth, 0);
    }

    /**
     * Search and find the best move in the current position.
     *
     * @param useIterativeDeepening weather to use iterative deepening
     * @param minDepth                 the depth to search to (not used if using iterative deepening)
     * @return the best move to DEPTH, according to the evaluator.
     */
    public Move getBestMove(boolean useIterativeDeepening, int minDepth, int timeCutoff) {
        GameModel currentGame = new GameModel(this.game);
        Evaluation bestEvalToLatestDepth = null;

        int currentDepth = useIterativeDeepening ? 1 : minDepth;
        do {
            // Asynchronously search for best move
            long startTime = System.currentTimeMillis();
            int finalDepth = currentDepth;
            CompletableFuture<Evaluation> futureEvaluation =
                    CompletableFuture.supplyAsync(() -> miniMax(currentGame, new AlphaBeta(), finalDepth));

            try {
                if (useIterativeDeepening && currentDepth > minDepth) {
                    // If using iterative deepening, wait for at most maxWaitTime for minimax to finish.
                    long maxWaitTime = timeCutoff - System.currentTimeMillis() + startTime;
                    maxWaitTime = maxWaitTime < 0 ? 1 : maxWaitTime;
                    bestEvalToLatestDepth = futureEvaluation.get(maxWaitTime, TimeUnit.MILLISECONDS);
                } else {
                    // else, wait however long it takes.
                    bestEvalToLatestDepth = futureEvaluation.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            } catch (TimeoutException e) {
                futureEvaluation.cancel(true);
                break;
            }

            currentDepth++;
        } while (useIterativeDeepening);

        System.out.println(bestEvalToLatestDepth);
        return bestEvalToLatestDepth == null ? null : bestEvalToLatestDepth.getMove();
    }

    /**
     * Get the evaluation of the current position to the given depth.
     * This is an implementation of the minimax algorithm to simulate
     * optimal play by both players.
     *
     * @param alphaBeta the AlphaBeta object used for pruning.
     * @param depth     the depth to search to, each depth is 1 ply.
     * @return the Evaluation of the current position assuming optimal play.
     */
    private Evaluation miniMax(GameModel game, AlphaBeta alphaBeta, int depth) {
        if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) {
            return evaluator.evaluate(game);
        }

        long hash = game.getZobristWithTimesMoved();
        boolean maximizingPlayer = game.getTurn() == 'w';
        Evaluation bestEval = maximizingPlayer ? Evaluation.WORST_EVALUATION : Evaluation.BEST_EVALUATION;

        // Search table for current position hash
        var tableValue = transpositionTable.get(hash);
        if (tableValue != null) {
            Evaluation tableEval = tableValue.getEvaluation();

            // If tableDepth is >= current depth use value
            if (tableEval.getDepth() == depth) {
                bestEval = tableEval;
                if (tableValue.isExact()) {
                    return bestEval;
                } else if (tableValue.isLower()) {
                    alphaBeta.betaMin(bestEval.getEvaluation());
                } else if (tableValue.isUpper()) {
                    alphaBeta.alphaMax(bestEval.getEvaluation());
                }
            }
        }

        // Search through all the sorted moves
        List<Move> sortedMoves = evaluator.getSortedMoves(game, tableValue == null ? null : tableValue.getEvaluation().getMove());
        boolean didBreak = false;
        for (Move move : sortedMoves) {
            // If we found a better path already, break.
            if (alphaBeta.betaLessThanAlpha()) {
                didBreak = true;
                break;
            }

            bestEval = getEvaluation(game, maximizingPlayer, bestEval, alphaBeta, move, depth);
        }

        // Add best Eval to transposition table.
        if (bestEval != Evaluation.BEST_EVALUATION && bestEval != Evaluation.WORST_EVALUATION) {
            tableValue = transpositionTable.get(hash);
            if (tableValue == null || tableValue.getEvaluation().getDepth() < bestEval.getDepth()) {
                if (didBreak) {
                    transpositionTable.put(hash, bestEval, maximizingPlayer ? LOWER : UPPER);
                } else {
                    transpositionTable.put(hash, bestEval, EXACT);
                }
            }
        }

        return bestEval;
    }

    /**
     * Evaluates a given move to the given depth. Calls the minimax
     * algorithm to evaluate the move.
     *
     * @param maximizingPlayer if the moving player is maximizing.
     * @param bestEval         the best move found so far.
     * @param alphaBeta        the AlphaBeta object, used for pruning.
     * @param move             the move to evaluate.
     * @param depth            the depth to search to.
     * @return the Evaluation of the given move.
     */
    private Evaluation getEvaluation(GameModel game, boolean maximizingPlayer, Evaluation bestEval,
                                     AlphaBeta alphaBeta, Move move, int depth) {
        // Make the given move.
        game.move(move);

        // Evaluate the position
        Evaluation moveEval = miniMax(game, new AlphaBeta(alphaBeta), depth - 1);

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

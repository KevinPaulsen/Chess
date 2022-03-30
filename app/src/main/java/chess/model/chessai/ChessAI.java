package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static chess.model.GameModel.IN_PROGRESS;
import static chess.model.chessai.Evaluation.*;

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
    private final HashMap<Long, Evaluation> transpositionTable;

    /**
     * The evaluator this class uses to evaluate positions, and moves.
     */
    private final Evaluator evaluator;

    /**
     * The game this AI is playing in.
     */
    private final GameModel game;

    private final ExecutorService executorService;

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
        this.transpositionTable = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(4);
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
     * @param minDepth              the depth to search to (not used if using iterative deepening)
     * @return the best move to DEPTH, according to the evaluator.
     */
    public Move getBestMove(boolean useIterativeDeepening, int minDepth, int timeCutoff) {
        GameModel currentGame = new GameModel(this.game);
        Evaluation bestEvalToLatestDepth = null;

        int currentDepth = useIterativeDeepening ? 1 : minDepth;
        long startTime = System.currentTimeMillis();
        do {
            // Asynchronously search for best move
            int finalDepth = currentDepth;
            Future<Evaluation> futureEvaluation = executorService.submit(() -> miniMax(currentGame, new AlphaBeta(), finalDepth));
            /*CompletableFuture<Evaluation> futureEvaluation =
                    CompletableFuture.supplyAsync(() -> miniMax(currentGame, new AlphaBeta(), finalDepth));//*/

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
        if (depth < 0) {
            System.out.println("HOW DID THIS HAPPEN?");
        }
        if (depth <= 0 || game.getGameOverStatus() != IN_PROGRESS) {
            return evaluator.evaluate(game);
        }

        long hash = game.getZobristWithTimesMoved();
        boolean maximizingPlayer = game.getTurn() == 'w';
        Evaluation bestEval = maximizingPlayer ? Evaluation.MIN_EVALUATION : Evaluation.MAX_EVALUATION;
        Move bestMove = null;

        Evaluation tableEval = transpositionTable.get(hash);
        // Search table for current position hash
        if (tableEval != null) {
            // If tableDepth is >= current depth use value
            if (tableEval.getDepth() >= depth || tableEval.getLoser() != Evaluation.NO_LOSER) {
                bestEval = tableEval;
                bestMove = tableEval.getMove();
                if (tableEval.isExact()) {
                    return bestEval;
                } else if (maximizingPlayer) {
                    alphaBeta.alphaMax(bestEval.getEvaluation());
                } else {
                    alphaBeta.betaMin(bestEval.getEvaluation());
                }
            }
        }

        // Search through all the sorted moves
        List<Move> sortedMoves = evaluator.getSortedMoves(game, bestMove);
        boolean didBreak = false;
        for (Move move : sortedMoves) {
            if (move == bestEval.getMove()) {
                continue;
            }

            // If we found a better path already, break.
            if (alphaBeta.betaLessThanAlpha()) {
                didBreak = true;
                break;
            }

            // Make the move
            game.move(move);

            // Evaluate the position
            Evaluation currentEval = miniMax(game, new AlphaBeta(alphaBeta), depth - 1);

            // Undo the move
            game.undoLastMove();

            if (maximizingPlayer) {
                // If maximizing player, set the best eval to the max
                if (bestEval.compareTo(currentEval) < 0) {
                    bestEval = currentEval;
                    bestMove = move;
                    alphaBeta.alphaMax(bestEval.getEvaluation());
                }
            } else if (bestEval.compareTo(currentEval) > 0) {
                // If minimizing player, set the best eval to the min
                bestEval = currentEval;
                bestMove = move;
                alphaBeta.betaMin(bestEval.getEvaluation());
            }
        }
        if (bestEval != tableEval) {
            bestEval = new Evaluation(bestMove, bestEval, didBreak ? (maximizingPlayer ? LOWER : UPPER) : EXACT);
        }

        // Add best Eval to transposition table.
        if (bestEval != Evaluation.MAX_EVALUATION && bestEval != Evaluation.MIN_EVALUATION) {
            tableEval = transpositionTable.get(hash);
            if (tableEval == null || tableEval.getDepth() < depth) {
                transpositionTable.put(hash, bestEval);
            }
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
            return beta < alpha;
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

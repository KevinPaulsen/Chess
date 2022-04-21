package chess.model.chessai;

import chess.Move;
import chess.model.GameModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static chess.model.GameModel.IN_PROGRESS;
import static chess.model.GameModel.WHITE;
import static chess.model.chessai.Evaluation.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

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
    private final Map<Long, Evaluation> transpositionTable;

    /**
     * The evaluator this class uses to evaluate positions, and moves.
     */
    private final Evaluator evaluator;

    /**
     * The game this AI is playing in.
     */
    private final GameModel game;

    private final ExecutorService executorService;
    private final boolean useTranspositionTable;
    private final boolean useIterativeDeepening;

    /**
     * Simple constructor that makes a new ChessAI with the given
     * evaluator and game.
     *
     * @param evaluator the evaluator this AI uses.
     * @param game      the game this AI is in.
     */
    public ChessAI(Evaluator evaluator, GameModel game, boolean useIterativeDeepening, boolean useTranspositionTable) {
        this.evaluator = evaluator;
        this.game = game;
        this.useIterativeDeepening = useIterativeDeepening;
        this.useTranspositionTable = useTranspositionTable;
        this.transpositionTable = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Returns the best move to the given depth. This does not use iterative deepening.
     *
     * @param depth the depth to search to
     * @return the best move in the position
     */
    public Move getBestMove(int depth) {
        return getBestMove(depth, 0);
    }

    /**
     * Search and find the best move in the current position.
     *
     * @param minDepth              the depth to search to (not used if using iterative deepening)
     * @param timeCutoff the max time to search for.
     * @return the best move to DEPTH, according to the evaluator.
     */
    public Move getBestMove(int minDepth, int timeCutoff) {
        GameModel currentGame = new GameModel(this.game);

        Evaluation bestEvalToLatestDepth = null;

        if (!useIterativeDeepening) {
            bestEvalToLatestDepth = miniMax(currentGame, new AlphaBeta(), minDepth);
        } else {
            // Do minimax iteratively up to minDepth
            long start = System.nanoTime();
            for (int depth = 1; depth <= minDepth; depth++) {
                bestEvalToLatestDepth = miniMax(currentGame, new AlphaBeta(), depth);
            }
            long end = System.nanoTime();

            // Continue search starting at minDepth + 1, until timeout
            IterativeDeepener deepener = new IterativeDeepener(bestEvalToLatestDepth, currentGame, minDepth);
            CompletableFuture<Void> future = CompletableFuture.runAsync(deepener, executorService);

            try {
                future.get(NANOSECONDS.convert(timeCutoff, MILLISECONDS) - end + start, NANOSECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                future.cancel(true);
            }
            deepener.kill();
            bestEvalToLatestDepth = deepener.bestEval;
        }
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
        boolean maximizingPlayer = game.getTurn() == WHITE;
        Evaluation bestEval = maximizingPlayer ? Evaluation.MIN_EVALUATION : Evaluation.MAX_EVALUATION;
        Move bestMove = null;

        Evaluation tableEval = transpositionTable.get(hash);
        // Search table for current position hash
        if (tableEval != null) {
            // If tableDepth is >= current depth use value
            bestMove = tableEval.getMove();
            if (tableEval.getDepth() == depth || tableEval.getLoser() != Evaluation.NO_LOSER) {
                bestEval = tableEval;
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
            bestEval = new Evaluation(bestEval, bestMove, didBreak ? (maximizingPlayer ? LOWER : UPPER) : EXACT);
        }

        // Add best Eval to transposition table.
        if (useTranspositionTable && bestEval != Evaluation.MAX_EVALUATION && bestEval != Evaluation.MIN_EVALUATION) {
            if (bestEval.isExact()) {
                transpositionTable.merge(hash, bestEval, (prev, next) -> prev.getDepth() < next.getDepth() ? next : prev);
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

    private class IterativeDeepener implements Runnable {

        private volatile boolean killed;
        private Evaluation bestEval;
        private final GameModel game;
        private final int startDepth;

        public IterativeDeepener(Evaluation bestEval, GameModel game, int startDepth) {
            this.killed = false;
            this.bestEval = bestEval;
            this.game = game;
            this.startDepth = startDepth;
        }

        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            int count = 1;
            while (!killed) {
                try {
                    runToDepth(startDepth + count++);
                } catch (InterruptedException ex) {
                    killed = true;
                }
            }
        }

        private void kill() {
            killed = true;
        }

        private void runToDepth(int depth) throws InterruptedException {
            bestEval = miniMax(game, new AlphaBeta(), depth);
        }
    }
}

package chess.model.chessai;

import chess.model.GameModel;
import chess.model.moves.Movable;
import chess.util.MaxSizeLRUCache;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

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

    private final boolean useTranspositionTable;
    private final boolean useIterativeDeepening;

    private final long positionsEvaluated;

    /**
     * Simple constructor that makes a new ChessAI with the given
     * evaluator and game.
     *
     * @param evaluator the evaluator this AI uses.
     * @param game      the game this AI is in.
     */
    public ChessAI(Evaluator evaluator, GameModel game, boolean useIterativeDeepening,
                   boolean useTranspositionTable) {
        this.evaluator = evaluator;
        this.game = game;
        this.useIterativeDeepening = useIterativeDeepening;
        this.useTranspositionTable = useTranspositionTable;
        this.transpositionTable = new MaxSizeLRUCache<>(1_000_000);
        positionsEvaluated = 0;
    }

    /**
     * Returns the best move to the given depth. This does not use iterative deepening.
     *
     * @param depth the depth to search to
     * @return the best move in the position
     */
    public Movable getBestMove(int depth) {
        return getBestMove(depth, 0);
    }

    /**
     * Search and find the best move in the current position.
     *
     * @param minDepth   the depth to search to (not used if using iterative deepening)
     * @param timeCutoff the max time to search for.
     * @return the best move to DEPTH, according to the evaluator.
     */
    public Movable getBestMove(int minDepth, int timeCutoff) {
        GameModel currentGame = new GameModel(this.game);

        Movable bestMove;

        if (useIterativeDeepening) {
            long start, end;

            IterativeDeepener deepener = new IterativeDeepener(currentGame, 1, minDepth);
            // Do minimax iteratively up to minDepth
            start = System.nanoTime();
            deepener.run();
            end = System.nanoTime();

            long totalTimeMills = MILLISECONDS.convert(end - start, NANOSECONDS);

            if (totalTimeMills < timeCutoff) {
                // Continue search starting at minDepth + 1, until timeout
                ExecutorService executor = Executors.newSingleThreadExecutor();
                deepener.startDepth = minDepth + 1;
                deepener.maxDepth = -1;

                try {
                    long nanoTimeCutoff = NANOSECONDS.convert(timeCutoff, MILLISECONDS);
                    executor.submit(deepener).get(nanoTimeCutoff - end + start, NANOSECONDS);
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                } catch (TimeoutException ignored) {
                    deepener.shutdown();
                }
                executor.shutdown();
            }

            bestMove = deepener.bestMove;
        } else {
            bestMove = new Searcher(currentGame, evaluator).getBestMove(minDepth);
        }

        return bestMove;
    }

    private class IterativeDeepener implements Runnable {

        private final Searcher searcher;
        private int startDepth;
        private int maxDepth;
        private volatile boolean canceled;
        private volatile Movable bestMove;

        public IterativeDeepener(GameModel game, int startDepth, int maxDepth) {
            this.searcher = new Searcher(game, evaluator);
            this.canceled = false;
            this.bestMove = null;
            this.startDepth = startDepth;
            this.maxDepth = maxDepth;
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
            int count = startDepth;
            while (!canceled && (maxDepth < 0 || count <= maxDepth)) {
                Movable move = searcher.getBestMove(count++);
                if (move != null) {
                    bestMove = move;
                }
            }
            System.out.println(bestMove);
        }

        public void shutdown() {
            canceled = true;
            searcher.cancel();
        }
    }
}

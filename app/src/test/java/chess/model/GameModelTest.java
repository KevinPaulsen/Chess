package chess.model;

import chess.Move;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static chess.model.GameModel.IN_PROGRESS;

public class GameModelTest {

    private static int countNumPositions(GameModel game, int depth, boolean printMove) {
        if (depth <= 0) {
            return 1;
        }
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves())) {
            if (game.move(move)) {
                int num = countNumPositions(game, depth - 1, false);
                if (printMove) {
                    System.out.printf("%s%s\t%6s: %7d\n", move.getStartingCoordinate(), move.getEndingCoordinate(), move, num);
                }
                sum += num;
                game.undoLastMove();
            }
        }
        return sum;
    }

    private static void runCountTest(GameModel gameModel, int[] expectedNumPositions) {
        for (int depth = 0; depth < expectedNumPositions.length; depth++) {
            int expected = expectedNumPositions[depth];
            int actual = countNumPositions(gameModel, depth, false);
            Assert.assertEquals("Wrong number of nodes found.", expected, actual);
        }
    }

    @Test
    public void testStartingPositionDepth() {
        GameModel game = new GameModel();
        int[] expectedNumPositions = {1, 20, 400, 8902, 197_281, 4_865_609};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testComplexPositionDepth() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int[] expectedNumPositions = {1, 44, 1_486, 62_379, 2_103_487/*, 89_941_194/**/};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testPawnBoardDepth() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int[] expectedNumPositions = {1, 13, 314, 3_598, 92_331, 1_001_929/*, 25_685_493/**/};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testHashDiffersByMove() {
        GameModel testGame1 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K w q - 2 2");
        GameModel testGame2 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K b q - 3 2");

        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                testGame1.getZobristHash(), testGame2.getZobristHash());
        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                testGame1.getZobristWithTimesMoved(), testGame2.getZobristWithTimesMoved());
    }

    @Test
    public void runSpeedTest() {
        GameModel complicated = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        AtomicLong counter = new AtomicLong(0);

        PositionCounter positionCounter = new PositionCounter(complicated, 12, counter);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        long start, end;
        start = end = System.nanoTime();
        try {
            executor.submit(positionCounter).get(60_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            end = System.nanoTime();
            positionCounter.shutdown();
        }
        executor.shutdown();

        long totalTime = end - start;
        System.out.printf("Reached %d positions in 60s\n", counter.get());
        System.out.printf("Approximately %d ns per position\n", totalTime / counter.get());
    }

    private static void runToDepth(GameModel game, int depth, AtomicLong counter) {
        counter.getAndIncrement();
        if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) return;
        List<Move> moves = game.getLegalMoves();
        //moves.sort(Comparator.comparing(Move::toString));
        for (Move move : moves) {
            game.move(move);
            runToDepth(game, depth - 1, counter);
            game.undoLastMove();
        }
    }

    private static class PositionCounter implements Runnable {
        private final GameModel game;
        private final int depth;
        private final AtomicLong counter;

        private volatile boolean shutdown;

        public PositionCounter(GameModel game, int depth, AtomicLong counter) {
            this.game = game;
            this.depth = depth;
            this.counter = counter;
            this.shutdown = false;
        }

        @Override
        public void run() {
            runToDepth(depth);
        }

        private void runToDepth(int depth) {
            counter.getAndIncrement();
            if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS || shutdown) return;
            List<Move> moves = game.getLegalMoves();
            //moves.sort(Comparator.comparing(Move::toString));
            for (Move move : moves) {
                game.move(move);
                runToDepth(depth - 1);
                game.undoLastMove();
            }
        }

        private void shutdown() {
            shutdown = true;
        }
    }
}
package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static chess.ChessCoordinate.*;
import static chess.model.GameModel.IN_PROGRESS;

public class GameModelTest {

    private static int countNumPositions(GameModel game, int depth, boolean printMove) {
        if (depth <= 0) {
            return 1;
        }
        int sum = 0;
        for (Move move : game.getLegalMoves()) {
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
            System.out.println();
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
    public void testHashDiffersByMove() {
        GameModel testGame1 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K w q - 2 2");
        GameModel testGame2 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K b q - 3 2");

        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                testGame1.getZobristHash(), testGame2.getZobristHash());
        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                testGame1.getZobristWithTimesMoved(), testGame2.getZobristWithTimesMoved());
    }

    @Test
    public void testMiddleWithFourCastle() {
        GameModel game = new GameModel("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        int[] expectedNumPositions = {1, 48, 2_039, 97_862, 4_085_603, 193_690_690/**/};
        runCountTest(game, expectedNumPositions);
    }

    private void debug(GameModel game, int i) {
        System.out.println(game.getFEN());
        System.out.println(countNumPositions(game, i - game.moveNum(), true));
    }

    @Test
    public void testPawnEnPassantTest() {
        GameModel game = new GameModel("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
        int[] expectedNumPositions = {1, 14, 191, 2_812, 43_238, 674_624, 11_030_083};
        runCountTest(game, expectedNumPositions);
    }
    @Test
    public void testMiddlePromotionAndChecks() {
        GameModel game = new GameModel("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        int[] expectedNumPositions = {1, 6, 264, 9_467, 422_333, 15_833_292};
        runCountTest(game, expectedNumPositions);
    }
    @Test
    public void testComplexMiddle1() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int[] expectedNumPositions = {1, 44, 1_486, 62_379, 2_103_487, 89_941_194};
        runCountTest(game, expectedNumPositions);
    }
    @Test
    public void testComplexMiddle2() {
        GameModel game = new GameModel("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
        int[] expectedNumPositions = {1, 46, 2_079, 89_890, 3_894_594, 164_075_551/**/};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void runSpeedTest() {
        /*
        Bitmap test v1: 3696 (run with profiler)
        Bitmap test v2: 2730 (run without profiler)
        Bitmap test v2: 3980 (run with profiler)
        Bitmap test v3: 2605
        Bitmap test v3: 2671 / 2678 / 2542
         */
        /*
        GameModel complicated = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        AtomicLong counter = new AtomicLong(0);

        PositionCounter positionCounter = new PositionCounter(complicated, 12, counter);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        long start, end;
        start = end = System.nanoTime();
        try {
            //executor.submit(positionCounter).get();
            executor.submit(positionCounter).get(120_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            end = System.nanoTime();
            positionCounter.shutdown();
        }
        executor.shutdown();

        long totalTime = end - start;
        System.out.printf("Reached %d positions in 60s\n", counter.get());
        System.out.printf("Approximately %d ns per position\n", totalTime / counter.get());//*/
    }

    private static void runToDepth(GameModel game, int depth, AtomicLong counter) {
        counter.getAndIncrement();
        if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS) return;
        MoveList moves = game.getLegalMoves();
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
            MoveList moves = game.getLegalMoves();
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
package chess.model;

import chess.model.moves.Movable;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static chess.model.GameModel.IN_PROGRESS;
import static java.util.concurrent.TimeUnit.*;

public class GameModelTest {

    private static long countNumPositions(GameModel game, int depth, boolean printMove) {
        if (depth <= 0) {
            return 1;
        } else if (depth == 1) {
            return game.getLegalMoves().size();
        }

        long sum = 0;
        for (Movable move : game.getLegalMoves()) {
            if (game.move(move)) {
                long num = countNumPositions(game, depth - 1, false);
                if (printMove) {
                    System.out.printf("%s%s\t%6s: %7d\n", move.getStartCoordinate(),
                                      move.getEndCoordinate(), move, num);
                }
                sum += num;
                game.undoLastMove();
            }
        }
        return sum;
    }

    private static void runCountTest(GameModel gameModel, long[] expectedNumPositions) {
        for (int depth = 0; depth < expectedNumPositions.length; depth++) {
            long expected = expectedNumPositions[depth];
            long start, end;

            start = System.nanoTime();
            long actual = countNumPositions(gameModel, depth, false);
            end = System.nanoTime();
            long ns = end - start;


            double seconds = ns / 1_000_000_000.0;
            ns -= SECONDS.toNanos((long) seconds);
            long posPerSec = (long) (actual / seconds);
            long ms = NANOSECONDS.toMillis(ns);
            ns -= MILLISECONDS.toNanos(ms);
            System.out.printf(
                    "Depth %d ply | %,13d positions | %2ds %3dms %,7dns | ~%,10d pos/sec\n", depth,
                    actual, (long) seconds, ms, ns, posPerSec);
            Assert.assertEquals("Wrong number of nodes found.", expected, actual);
        }
    }

    private static void runToDepth(GameModel game, int depth, AtomicLong counter) {
        counter.getAndIncrement();
        if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS)
            return;
        MoveList moves = game.getLegalMoves();
        //moves.sort(Comparator.comparing(NormalMove::toString));
        for (Movable move : moves) {
            game.move(move);
            runToDepth(game, depth - 1, counter);
            game.undoLastMove();
        }
    }

    @Test
    public void testStartingPositionDepth() {
        GameModel game = new GameModel();
        long[] expectedNumPositions = {1, 20, 400, 8902, 197_281, 4_865_609, 119_060_324};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testHashDiffersByMove() {
        GameModel testGame1 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K w q - 2 2");
        GameModel testGame2 = new GameModel("r6k/5prp/3R4/4B3/8/8/7P/7K b q - 3 2");

        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                               testGame1.getZobristHash(), testGame2.getZobristHash());
        Assert.assertNotEquals("Same position but different moves evaluated to same hash.",
                               testGame1.getZobristWithTimesMoved(),
                               testGame2.getZobristWithTimesMoved());
    }

    @Test
    public void testMiddleWithFourCastle() {
        GameModel game = new GameModel(
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        long[] expectedNumPositions = {1, 48, 2_039, 97_862, 4_085_603, 193_690_690};
        //debug(game, 3);
        runCountTest(game, expectedNumPositions);
    }

    private void debug(GameModel game, int i) {
        //game.move(new NormalMove(Piece.WHITE_BISHOP, G5.getBitMask(), F4.getBitMask()));
        //game.move(new NormalMove(Piece.BLACK_KING, G8.getBitMask(), H8.getBitMask()));
        //game.move(new NormalMove(Piece.WHITE_KING, G1.getBitMask(), H1.getBitMask()));
        //game.undoLastMove();
        //game.move(new NormalMove(Piece.BLACK_ROOK, F8, E8));
        System.out.println(game.getFEN());
        System.out.println(countNumPositions(game, i - game.moveNum(), true));
    }

    @Test
    public void testPawnEnPassantTest() {
        GameModel game = new GameModel("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
        long[] expectedNumPositions = {1, 14, 191, 2_812, 43_238, 674_624, 11_030_083, 178_633_661};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testMiddlePromotionAndChecks() {
        GameModel game = new GameModel(
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        long[] expectedNumPositions = {1, 6, 264, 9_467, 422_333, 15_833_292, 706_045_033};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testComplexMiddle1() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        long[] expectedNumPositions = {1, 44, 1_486, 62_379, 2_103_487, 89_941_194, 3_048_196_529L};
        runCountTest(game, expectedNumPositions);
    }

    @Test
    public void testComplexMiddle2() {
        GameModel game = new GameModel(
                "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
        long[] expectedNumPositions = {1, 46, 2_079, 89_890, 3_894_594, 164_075_551};
        //debug(game, 4);
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
        Magic Bitboard test: 638
         */
        GameModel complicated = new GameModel(
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
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
        long timeSecs = SECONDS.convert(totalTime, NANOSECONDS);
        System.out.printf("Reached %d positions in %ds\n", counter.get(), timeSecs);
        System.out.printf("Approximately %d positions per second\n", counter.get() / timeSecs);
        System.out.printf("Approximately %d ns per position\n", totalTime / counter.get());
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
            if (depth == 0 || game.getGameOverStatus() != IN_PROGRESS || shutdown)
                return;
            MoveList moves = game.getLegalMoves();
            //moves.sort(Comparator.comparing(NormalMove::toString));
            for (Movable move : moves) {
                game.move(move);
                runToDepth(depth - 1);
                game.undoLastMove();
            }
        }

        private void shutdown() {
            //game.printTimes();
            shutdown = true;
        }
    }
}
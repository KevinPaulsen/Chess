package chess.model;

import chess.Move;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
}
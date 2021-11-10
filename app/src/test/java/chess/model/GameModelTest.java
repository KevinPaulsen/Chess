package chess.model;

import chess.Move;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GameModelTest {

    public static int countNumPositions1(GameModel game, int depth) {
        System.out.println();
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves())) {
            if (game.move(move)) {
                int num = countNumPositions(game, depth - 1);
                System.out.printf("%s%s\t%6s: %7d\n", move.getStartingCoordinate(), move.getEndingCoordinate(), move, num);
                sum += num;
                game.undoMove(move);
            }
        }

        System.out.println(game.getFEN());
        return sum;

    }

    private static int countNumPositions(GameModel game, int depth) {
        if (depth <= 0) {
            return 1;
        }
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves())) {
            if (game.move(move)) {
                sum += countNumPositions(game, depth - 1);
                game.undoMove(move);
            }
        }
        return sum;
    }

    @Test
    public void testStartingPositionDepth0() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testStartingPositionDepth1() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 20, numPositions);
    }

    @Test
    public void testStartingPositionDepth2() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 400, numPositions);
    }

    @Test
    public void testStartingPositionDepth3() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 8902, numPositions);
    }

    @Test
    public void testStartingPositionDepth4() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 197_281, numPositions);
    }

    @Test
    public void testStartingPositionDepth5() {
        GameModel game = new GameModel();
        int numPositions = countNumPositions(game, 5);
        Assert.assertEquals("Wrong number of nodes found.", 4_865_609, numPositions);
    }

    @Test
    public void testComplexPositionDepth0() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        System.out.println(game.getFEN());
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testComplexPositionDepth1() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 44, numPositions);
    }

    @Test
    public void testComplexPositionDepth2() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 1_486, numPositions);
    }

    @Test
    public void testComplexPositionDepth3() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 62_379, numPositions);
    }

    @Test
    public void testComplexPositionDepth4() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 2_103_487, numPositions);
    }

    @Test
    public void testComplexPositionDepth5() {
        GameModel game = new GameModel("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 0 1");
        int numPositions = countNumPositions(game, 5);
        Assert.assertEquals("Wrong number of nodes found.", 89_941_194, numPositions);
    }

    @Test
    public void testPawnBoardDepth0() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testPawnBoardDepth1() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 13, numPositions);
    }

    @Test
    public void testPawnBoardDepth2() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 314, numPositions);
    }

    @Test
    public void testPawnBoardDepth3() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 3_598, numPositions);
    }

    @Test
    public void testPawnBoardDepth4() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 92_331, numPositions);
    }

    @Test
    public void testPawnBoardDepth5() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        //game.move(BoardModel.getChessCoordinate('e', 2), BoardModel.getChessCoordinate('e', 4), Piece.WHITE_QUEEN);
        //game.move(BoardModel.getChessCoordinate('h', 5), BoardModel.getChessCoordinate('d', 1), Piece.WHITE_QUEEN);
        int numPositions = countNumPositions(game, 5);
        Assert.assertEquals("Wrong number of nodes found.", 1_001_929, numPositions);
    }

    @Test
    public void testPawnBoardDepth6() {
        GameModel game = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        int numPositions = countNumPositions(game, 6);
        Assert.assertEquals("Wrong number of nodes found.", 25_685_493, numPositions);
    }

    /*@SuppressWarnings("all")
    @Test
    public void privateTest() {
        GameModel game = new GameModel("r1bqkbnr/pppp1ppp/2n5/3Pp3/8/8/PPP1PPPP/RNBQKBNR w KQkq e6 0 3");
        System.out.println(countNumPositions(game, 1));
    }// */
}
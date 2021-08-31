package chess.model;

import chess.Move;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static chess.model.ChessBoardFactory.*;

public class GameModelTest {

    private final static int[][] TEST_BOARD_COMPLEX = {
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
            {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_BISHOP, W_PAWN, EMPTY, W_BISHOP, EMPTY, B_PAWN, EMPTY, B_BISHOP},
            {W_QUEEN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, B_QUEEN},
            {W_KING, W_KNIGHT, EMPTY, EMPTY, EMPTY, EMPTY, B_BISHOP, EMPTY},
            {EMPTY, B_KNIGHT, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
    };

    private final static int[][] TEST_BOARD_PAWNS = {
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, W_KING, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, W_PAWN, EMPTY, EMPTY, B_QUEEN, EMPTY, EMPTY, EMPTY},
    };
    private static final int A = 0;
    private static final int B = 1;
    private static final int C = 2;
    private static final int D = 3;
    private static final int E = 4;
    private static final int F = 5;
    private static final int G = 6;
    private static final int H = 7;

    public static int countNumPositions1(GameModel game, int depth) {
        System.out.println();
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves())) {
            if (game.move(move)) {
                int num = countNumPositions(game, depth - 1);
                System.out.println(move.toString() + ": " + num);
                sum += num;
                game.undoMove(move);
            }
        }

        System.out.println();
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
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testComplexPositionDepth1() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 44, numPositions);
    }

    @Test
    public void testComplexPositionDepth2() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 1_486, numPositions);
    }

    @Test
    public void testComplexPositionDepth3() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 62_379, numPositions);
    }

    @Test
    public void testComplexPositionDepth4() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 2_103_487, numPositions);
    }

    @Test
    public void testPawnBoardDepth0() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testPawnBoardDepth1() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 13, numPositions);
    }

    @Test
    public void testPawnBoardDepth2() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 314, numPositions);
    }

    @Test
    public void testPawnBoardDepth3() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 3_598, numPositions);
    }

    @Test
    public void testPawnBoardDepth4() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 92_331, numPositions);
    }

    @Test
    public void testPawnBoardDepth5() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 5);
        Assert.assertEquals("Wrong number of nodes found.", 1_001_929, numPositions);
    }

    @Test
    public void testPawnBoardDepth6() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_PAWNS), 'w',
                false, false, false, false, null);
        int numPositions = countNumPositions(game, 6);
        Assert.assertEquals("Wrong number of nodes found.", 25_685_493, numPositions);
    }

    @SuppressWarnings("all")
    @Test
    public void privateTest() {
        /*GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_COMPLEX), 'w',
                true, true, false, false, null);//
        //GameModel game = new GameModel();

        long start = System.currentTimeMillis();
        System.out.println("Positions: " + countNumPositions1(game, 5));
        long end = System.currentTimeMillis();
        System.out.println("" + (end - start) + " ms");// */
    }
}
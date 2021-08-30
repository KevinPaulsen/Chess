package chess.model;

import chess.Move;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static chess.model.ChessBoardFactory.*;
import static org.junit.Assert.*;

public class GameModelTest {


    private final static int[][] TEST_BOARD_1 = {
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY, EMPTY},
            {EMPTY, EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {W_KING, EMPTY, EMPTY, W_PAWN, EMPTY, B_PAWN, EMPTY, EMPTY},
            {W_ROOK, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, W_PAWN, B_PAWN, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, B_KNIGHT, EMPTY, B_BISHOP, EMPTY, B_PAWN, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, W_PAWN, W_KNIGHT, EMPTY, B_ROOK, EMPTY},
    };

    private final static int[][] TEST_BOARD_2 = {
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
            {W_KNIGHT, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KNIGHT},
            {W_BISHOP, W_PAWN, EMPTY, W_BISHOP, EMPTY, B_PAWN, EMPTY, B_BISHOP},
            {W_QUEEN, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, W_PAWN, B_QUEEN},
            {W_KING, W_KNIGHT, EMPTY, EMPTY, EMPTY, EMPTY, B_BISHOP, EMPTY},
            {EMPTY, B_KNIGHT, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_KING},
            {EMPTY, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, EMPTY},
            {W_ROOK, W_PAWN, EMPTY, EMPTY, EMPTY, EMPTY, B_PAWN, B_ROOK},
    };

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
    public void testComplexPositionDepth0() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 0);
        Assert.assertEquals("Wrong number of nodes found.", 1, numPositions);
    }

    @Test
    public void testComplexPositionDepth1() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 1);
        Assert.assertEquals("Wrong number of nodes found.", 44, numPositions);
    }

    @Test
    public void testComplexPositionDepth2() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 2);
        Assert.assertEquals("Wrong number of nodes found.", 1_486, numPositions);
    }

    @Test
    public void testComplexPositionDepth3() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 3);
        Assert.assertEquals("Wrong number of nodes found.", 62_379, numPositions);
    }

    @Test
    public void testComplexPositionDepth4() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);
        int numPositions = countNumPositions(game, 4);
        Assert.assertEquals("Wrong number of nodes found.", 2_103_487, numPositions);
    }

    @Test
    public void privateTest() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);

        long start = System.currentTimeMillis();
        MoveGenerator generator = new MoveGenerator(game);
        System.out.println(generator.generateMoves().size());
        //System.out.println("Positions: " + countNumPositions1(game, 3));
        long end = System.currentTimeMillis();
        System.out.println("" + (end - start) + " ms");
    }

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
}
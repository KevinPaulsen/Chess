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


    private final int[][] expectedAttackers = {
            {0, 1, 2, 0, 0, 2, 1, 0},
            {1, 1, 2, 0, 0, 2, 1, 1},
            {1, 1, 3, 0, 0, 3, 1, 1},
            {1, 4, 2, 0, 0, 2, 4, 1},
            {1, 4, 2, 0, 0, 2, 4, 1},
            {1, 1, 3, 0, 0, 3, 1, 1},
            {1, 1, 2, 0, 0, 2, 1, 1},
            {0, 1, 2, 0, 0, 2, 1, 0},
    };
    @Test
    public void constructorTest() {
        GameModel gameModel = new GameModel();
        Square[][] squares = gameModel.getBoard().getPieceArray();

        // Test that each piece is attacking right number of squares
        for (int file = 0; file < squares.length; file++) {
            for (int row = 0; row < squares.length; row++) {
                int numAttackers = squares[file][row].numAttackers('w') + squares[file][row].numAttackers('b');
                assertEquals("Square " + BoardModel.getChessCoordinate(file, row) + " has wrong number of attackers", expectedAttackers[file][row], numAttackers);
            }
        }

        for (Square[] file: gameModel.getBoard().getPieceArray()) {
            for (Square square : file) {
                Piece piece = square.getPiece();
                if (piece != null) {
                    if (piece instanceof Pawn) {
                        assertEquals("Pawn on " + piece.getCoordinate() + "has wrong number of moves", 2, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    } else if (piece instanceof Knight) {
                        assertEquals("Knight on " + piece.getCoordinate() + "has wrong number of moves", 2, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    } else {
                        assertEquals("Piece on " + piece.getCoordinate() + "has wrong number of moves", 0, piece.updateLegalMoves(gameModel.getBoard(), gameModel.getLastMove()).size());
                    }
                }
            }
        }
    }

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

    public static void main() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2), 'w',
                true, true, false, false, null);

        game.move(BoardModel.getChessCoordinate(0, 1), BoardModel.getChessCoordinate(0, 3));
        game.move(BoardModel.getChessCoordinate(0, 6), BoardModel.getChessCoordinate(0, 5));
        //game.move(BoardModel.getChessCoordinate(3, 0), BoardModel.getChessCoordinate(3, 3));
        //game.move(BoardModel.getChessCoordinate(3, 0), BoardModel.getChessCoordinate(4, 2));

        long start = System.currentTimeMillis();

        System.out.println("Positions: " + countNumPositions1(game, 3));

        long end = System.currentTimeMillis();
        System.out.println("" + (end - start) + " ms");
    }

    public static int countNumPositions1(GameModel game, int depth) {
        System.out.println();
        int sum = 0;
        for (Move move : List.copyOf(game.getLegalMoves(game.getTurn()))) {
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
        for (Move move : List.copyOf(game.getLegalMoves(game.getTurn()))) {
            if (game.move(move)) {
                sum += countNumPositions(game, depth - 1);
                game.undoMove(move);
            }
        }
        return sum;
    }
}
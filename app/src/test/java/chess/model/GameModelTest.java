package chess.model;

import chess.Move;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
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
    public void moveAndUndo() {
        GameModel game = new GameModel(ChessBoardFactory.createChessBoard(TEST_BOARD_2, true, false), 'w');

        game.move(BoardModel.getChessCoordinate(0, 1), BoardModel.getChessCoordinate(0, 3));
        game.move(BoardModel.getChessCoordinate(0, 6), BoardModel.getChessCoordinate(0, 5));
        //game.move(BoardModel.getChessCoordinate(3, 0), BoardModel.getChessCoordinate(3, 3));
        //game.move(BoardModel.getChessCoordinate(3, 0), BoardModel.getChessCoordinate(4, 2));

        long start = System.currentTimeMillis();

        System.out.println("Positions: " + countNumPositions1(game, 3));

        long end = System.currentTimeMillis();
        System.out.println("" + (end - start) + " ms");
    }

    // rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8
    // rnbq1k1r/pp1Pbppp/2p5/8/2B5/P7/1PP1NnPP/RNBQK2R b KQ - 0 8
    // rnbq1k1r/pp1Pbppp/2p5/8/2B5/P7/1PP1N1PP/RNBnK2R w KQ - 0 9

    public int countNumPositions1(GameModel game, int depth) {
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

        int test = 9_329;

        System.out.println();
        return sum;

    }

    private int countNumPositions(GameModel game, int depth) {
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